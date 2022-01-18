/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.manager.script;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.obbylang.project.PackageResolver;
import com.clubobsidian.obbylang.project.ProjectPackage;
import com.clubobsidian.obbylang.util.ChatColor;
import com.clubobsidian.obbylang.util.ResourceUtil;
import io.lettuce.core.SslOptions;
import javassist.ClassClassPath;
import javassist.ClassPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptManager {

    private boolean loaded;
    private final ScriptEngine engine;
    private final Compilable compilableEngine;
    private final Map<String, CompiledScript> scripts = new ConcurrentHashMap<>();
    private final ObbyLangPlugin plugin;
    private final Path dependenciesDirectory;
    private final Path scriptDirectory;
    private final Path projectDirectory;
    private final AddonManager addonManager;

    @Inject
    private ScriptManager(ObbyLangPlugin plugin, AddonManager addonManager) {
        ClassLoader cl = plugin.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        System.setProperty("nashorn.args", "--language=es6");
        this.engine = new NashornScriptEngineFactory().getScriptEngine();
        this.compilableEngine = (Compilable) engine;
        this.plugin = plugin;
        this.dependenciesDirectory = Paths.get(plugin.getDataFolder().getPath(), "dependencies");
        this.scriptDirectory = Paths.get(plugin.getDataFolder().getPath(), "scripts");
        this.projectDirectory = Paths.get(plugin.getDataFolder().getPath(), "projects");
        this.addonManager = addonManager;
    }

    public boolean load() {
        if(!this.loaded) {
            this.loadClassPool();
            this.createDependencies();
            this.loadProjects();
            this.loadScripts();
            this.loaded = true;
            return true;
        }
        return false;
    }

    private void loadClassPool() {
        ClassPool.getDefault().insertClassPath(new ClassClassPath(ScriptObjectMirror.class));
        ClassPool.getDefault().insertClassPath(new ClassClassPath(ScriptWrapper.class));
        ClassPool.getDefault().insertClassPath(new ClassClassPath(ScriptWrapper[].class));
        ClassPool.getDefault().insertClassPath(new ClassClassPath(ListenerManager.class));
        ClassPool.getDefault().insertClassPath(new ClassClassPath(Field.class));
        ClassPool.getDefault().insertClassPath(new ClassClassPath(Map.class));
    }

    private CompiledScript createCompiledScript(File file) {
        try(FileReader reader = new FileReader(file)) {
            return this.compilableEngine.compile(reader);
        } catch(IOException | ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createDependencies() {
        try {
            Files.createDirectories(this.dependenciesDirectory);
        } catch(IOException e) {
            e.printStackTrace();
        }
        File requireFile = new File(this.dependenciesDirectory.toFile(), "require.js");
        ResourceUtil.save("require.js", requireFile, true);
    }

    private void createProjectDirectory() {
        try {
            Files.createDirectories(this.projectDirectory);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRequire(ScriptContext context) {
        try {
            File requireFile = new File(this.dependenciesDirectory.toFile(), "require.js");
            this.createCompiledScript(requireFile).eval(context);
        } catch(ScriptException e) {
            e.printStackTrace();
        }
    }

    private void loadProjects() {
        this.createProjectDirectory();
        File directoryFile = this.projectDirectory.toFile();
        for(File file : directoryFile.listFiles()) {
            if(!file.isDirectory()) {
                continue;
            }
            String fileName = file.getName();
            Optional<ProjectPackage> packageOpt = PackageResolver.resolve(file);
            if(packageOpt.isPresent()) {
                ProjectPackage project = packageOpt.get();
                String name = project.getName();
                if(name != null) {
                    String main = project.getMain();
                    System.out.println("main: " + main);
                    File projectDirectory = new File(directoryFile, file.getName());
                    if(main != null && new File(projectDirectory, main).exists()) {
                        File mainFile = new File(projectDirectory, main);
                        ScriptContext context = this.createContext(name);
                        context.setAttribute("PROJECT_DIRECTORY", projectDirectory, ScriptContext.ENGINE_SCOPE);
                        this.loadRequire(context);
                        this.loadScript(name, context, mainFile, null);
                    } else {
                        this.plugin.getLogger().log(Level.SEVERE, "No main file found for: " + fileName);
                    }
                } else {
                    this.plugin.getLogger().log(Level.SEVERE, "No name found in project: " + fileName);
                }
            } else {
                this.plugin.getLogger().log(Level.SEVERE, "No package file found for: " + fileName);
            }
        }
    }

    private void loadScripts() {
        try {
            Files.createDirectories(this.scriptDirectory);
        } catch(IOException e) {
            e.printStackTrace();
        }

        Collection<File> fileCollection = FileUtils.listFiles(this.scriptDirectory.toFile(), new String[]{"js"}, true);
        File[] files = fileCollection.toArray(new File[fileCollection.size()]);

        Collection<File> sortedScripts = Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        for(File file : sortedScripts) {
            try {
                this.loadScript(file, null);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Path getScriptDirectory() {
        return this.scriptDirectory;
    }

    public CompiledScript getScript(String script) {
        return this.scripts.get(script);
    }

    public boolean isScriptLoaded(String script) {
        return this.scripts.get(script) != null;
    }

    public String getScriptListString() {
        StringBuilder builder = new StringBuilder();
        Set<String> scripts = this.scripts.keySet();

        for(File file : FileUtils.listFiles(this.scriptDirectory.toFile(), new String[]{"js", "dis"}, true)) {
            String name = file.getName();
            String strippedName = name.replace(".dis", "").replace(".js", "");
            if(name.endsWith(".dis")) {
                builder.append(ChatColor.RED);
            } else if(name.endsWith(".js")) {
                if(scripts.contains(name)) {
                    builder.append(ChatColor.GREEN);
                } else {
                    builder.append(ChatColor.GRAY);
                }
            }
            builder.append(strippedName);
            builder.append(ChatColor.WHITE + ", ");
        }
        return builder.substring(0, builder.toString().length() - 2);
    }

    public List<String> getScriptNamesRaw() {
        List<String> scriptNames = new ArrayList<>();
        for(String str : this.scripts.keySet()) {
            scriptNames.add(str);
        }
        return scriptNames;
    }

    public List<String> getScriptNames() {
        List<String> scriptNames = new ArrayList<>();
        for(String str : this.scripts.keySet()) {
            scriptNames.add(str.replace(".js", "").replace(".dis", ""));
        }
        return scriptNames;
    }

    public boolean unloadScript(String className) {
        return this.unloadScript(className, null);
    }

    public boolean unloadScript(String className, Pipe pipe) {
        if(!className.endsWith(".js")) {
            className += ".js";
        }
        CompiledScript script = this.scripts.get(className);
        if(script == null) {
            return false;
        }

        for(Object addon : this.addonManager.getAddons().values()) {
            if(addon instanceof RegisteredManager) {
                RegisteredManager manager = (RegisteredManager) addon;
                manager.unregister(className);
            }
        }

        this.scripts.remove(className);
        return true;
    }

    public boolean loadScript(String location) {
        return this.loadScript(location, null);
    }

    public boolean loadScript(String location, Pipe pipe) {
        if(!location.endsWith(".js")) {
            location += ".js";
        }
        File file = new File(this.scriptDirectory.toFile(), location);
        return this.loadScript(file, pipe);
    }

    private boolean loadScript(File file, Pipe pipe) {
        return this.loadScript(null, file, pipe);
    }

    private boolean loadScript(String projectName, File file, Pipe pipe) {
        return this.loadScript(projectName, null, file, pipe);
    }

    private boolean loadScript(String projectName, ScriptContext context, File file, Pipe pipe) {
        try {
            String name = projectName != null ? projectName : file.getName();
            this.plugin.getLogger().info("Loading: " + name);
            return this.loadScriptSilent(projectName, context, file, pipe);
        } catch(Exception ex) {
            if(pipe != null) {
                this.sendStacktrace(ex, pipe);
            }
        }
        return false;
    }

    private boolean loadScriptSilent(String projectName, ScriptContext context, File file, Pipe pipe) throws Exception {
        String name = projectName != null ? projectName : file.getName();
        if(!file.exists() || this.scripts.containsKey(name)) {
            return false;
        }
        try {
            CompiledScript script = this.createCompiledScript(file);
            this.scripts.put(name, script);
            ScriptContext evalContext = context != null ? context : this.createContext(name);
            script.eval(evalContext);
            return true;
        } catch(Exception ex) {
            this.unloadScript(name, pipe);
            ex.printStackTrace();
            throw(ex);
        }
    }

    private ScriptContext createContext(String scriptName) {
        ScriptContext context = new SimpleScriptContext();
        Bindings bindings = this.engine.createBindings();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("owner", scriptName, ScriptContext.ENGINE_SCOPE);
        Iterator<Entry<String, Object>> it = this.addonManager.getAddons().entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Object> next = it.next();
            String key = next.getKey();
            Object value = next.getValue();
            context.setAttribute(key, value, ScriptContext.ENGINE_SCOPE);
        }
        return context;
    }

    public boolean reloadScript(String location, Pipe pipe) {
        if(!location.endsWith(".js"))
            location = location + ".js";

        File file = new File(this.scriptDirectory.toFile(), location);
        if(!file.exists())
            return false;

        boolean unload = this.unloadScript(file.getName(), pipe);
        if(!unload)
            return false;

        return this.loadScript(location, pipe);
    }

    public boolean enabledScript(String location) {
        return this.enableScript(location, null);
    }

    public boolean enableScript(String location, Pipe pipe) {
        if(!location.contains(".js")) {
            location += ".js";
        }
        File file = new File(this.scriptDirectory.toFile(), location);
        if(file.getName().endsWith(".dis")) {
            File toCopy = new File(this.scriptDirectory.toFile(), location.replace(".dis", ""));
            try {
                Files.copy(Paths.get(file.toURI()), Paths.get(toCopy.toURI()));
                file.delete();
                return this.loadScript(location.replace(".dis", ""), pipe);
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }
        } else if(!file.exists()) {
            File original = file;
            file = new File(this.scriptDirectory.toFile(), location + ".dis");
            if(!file.exists()) {
                return false;
            } else {
                try {
                    Files.copy(Paths.get(file.toURI()), Paths.get(original.toURI()));
                    file.delete();
                    return this.loadScript(location.replace(".dis", ""));
                } catch(IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public boolean disableScript(String location) {
        return this.disableScript(location, null);
    }

    public boolean disableScript(String location, Pipe pipe) {
        if(!location.endsWith(".js")) {
            location += ".js";
        }
        File file = new File(this.scriptDirectory.toFile(), location);
        if(!file.exists()) {
            return false;
        } else {
            File toCopy = new File(this.scriptDirectory.toFile(), location + ".dis");
            try {
                this.unloadScript(location, pipe);
                Files.copy(Paths.get(file.toURI()), Paths.get(toCopy.toURI()));
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }
            file.delete();
            return true;
        }
    }

    private void sendStacktrace(Exception e, Pipe pipe) {
        String message = e.getMessage();
        if(message.contains("<eval>")) {
            pipe.out(message);
        } else {
            String st = ExceptionUtils.getStackTrace(e);
            Pattern pattern = Pattern.compile("(?<=program\\(<eval>:)(\\d*)(?=\\))");
            Matcher matcher = pattern.matcher(st);
            if(matcher.find()) {
                message += " at line " + matcher.group();
            }
            pipe.out(message);
        }
    }
}