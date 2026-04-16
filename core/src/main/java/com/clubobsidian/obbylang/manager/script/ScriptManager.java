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

import com.caoccao.qjs4j.core.*;
import com.clubobsidian.obbylang.compat.JavaObjectRegistry;
import com.clubobsidian.obbylang.compat.NashornJavaCompat;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.obbylang.util.ChatColor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptManager {

    private boolean loaded;
    private final Path directory;
    private final JSRuntime engine;
    private final Map<String, JSContext> scripts = new ConcurrentHashMap<>();
    private final Map<String, JavaObjectRegistry> registries = new ConcurrentHashMap<>();

    private final ObbyLangPlugin plugin;
    private final AddonManager addonManager;

    @Inject
    private ScriptManager(ObbyLangPlugin plugin, AddonManager addonManager) {
        ClassLoader cl = plugin.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        this.plugin = plugin;
        this.directory = Paths.get(plugin.getDataFolder().getPath(), "scripts");
        this.engine = new JSRuntime();
        this.addonManager = addonManager;
    }

    public boolean load() {
        if(!this.loaded) {
            this.loadScripts();
            this.loaded = true;
            return true;
        }
        return false;
    }

    private JSContext createContext(String scriptName) {
        return this.engine.createContext();
    }

    private void loadScripts() {
        try {
            Files.createDirectories(this.directory);
        } catch(IOException e) {
            e.printStackTrace();
        }

        Collection<File> fileCollection = FileUtils.listFiles(this.directory.toFile(), new String[]{"js"}, true);
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

    public Path getDirectory() {
        return this.directory;
    }

    public JSContext getScript(String script) {
        return this.scripts.get(script);
    }

    public JavaObjectRegistry getRegistry(String script) {
        return this.registries.get(script);
    }

    public boolean isScriptLoaded(String script) {
        return this.scripts.get(script) != null;
    }

    public String getScriptListString() {
        StringBuilder builder = new StringBuilder();
        Set<String> scripts = this.scripts.keySet();

        for(File file : FileUtils.listFiles(this.directory.toFile(), new String[]{"js", "dis"}, true)) {
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
        JSContext script = this.scripts.get(className);
        if(script == null) {
            return false;
        }

        for(Object addon : this.addonManager.getAddons().values()) {
            if(addon instanceof RegisteredManager) {
                RegisteredManager manager = (RegisteredManager) addon;
                manager.unregister(className);
            }
        }

        JSContext removed = this.scripts.remove(className);
        if (removed != null) {
            removed.close();
        }
        JavaObjectRegistry registry = this.registries.remove(className);
        if (registry != null) {
            registry.clear();
        }
        return removed != null;
    }

    public boolean loadScript(String location) {
        return this.loadScript(location, null);
    }

    public boolean loadScript(String location, Pipe pipe) {
        if(!location.endsWith(".js")) {
            location += ".js";
        }
        File file = new File(this.directory.toFile(), location);
        return this.loadScript(file, pipe);
    }

    private boolean loadScript(File file, Pipe pipe) {
        String scriptName = file.getName();
        if(!file.exists() || this.scripts.containsKey(scriptName)) {
            return false;
        }
        try {
            this.plugin.getLogger().info("Loading: " + scriptName);
            JSContext context = this.createContext(scriptName);
            JavaObjectRegistry registry = this.addBindingsToContext(context, scriptName);
            this.scripts.put(scriptName, context);
            this.registries.put(scriptName, registry);
            String source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            context.eval(source, file.getName(), false);
            return true;
        } catch(Exception e) {
            this.unloadScript(scriptName, pipe);
            e.printStackTrace();
            if(pipe != null) {
                this.sendStacktrace(e, pipe);
            }
        }
        return false;
    }

    private JavaObjectRegistry addBindingsToContext(JSContext context, String scriptName) {
        context.getGlobalObject().set("owner", new JSString(scriptName));
        JavaObjectRegistry registry = NashornJavaCompat.install(context);
        for (Entry<String, Object> next : this.addonManager.getAddons().entrySet()) {
            String key = next.getKey();
            Object value = next.getValue();
            context.getGlobalObject().set(key, NashornJavaCompat.wrapJavaObject(context, registry, value));
        }
        return registry;
    }

    public boolean reloadScript(String location, Pipe pipe) {
        if(!location.endsWith(".js"))
            location = location + ".js";

        File file = new File(this.directory.toFile(), location);
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
        File file = new File(this.directory.toFile(), location);
        if(file.getName().endsWith(".dis")) {
            File toCopy = new File(this.directory.toFile(), location.replace(".dis", ""));
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
            file = new File(this.directory.toFile(), location + ".dis");
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
        File file = new File(this.directory.toFile(), location);
        if(!file.exists()) {
            return false;
        } else {
            File toCopy = new File(this.directory.toFile(), location + ".dis");
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