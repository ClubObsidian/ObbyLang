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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptManager {

    private boolean loaded;
    private final Path directory;
    private final Path projectsDirectory;
    private final JSRuntime engine;
    private final Map<String, JSContext> scripts = new ConcurrentHashMap<>();
    private final Map<String, JavaObjectRegistry> registries = new ConcurrentHashMap<>();
    private final Map<String, JSContext> projects = new ConcurrentHashMap<>();
    private final Map<String, JavaObjectRegistry> projectRegistries = new ConcurrentHashMap<>();

    private final ObbyLangPlugin plugin;
    private final AddonManager addonManager;

    @Inject
    private ScriptManager(ObbyLangPlugin plugin, AddonManager addonManager) {
        ClassLoader cl = plugin.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        this.plugin = plugin;
        this.directory = Paths.get(plugin.getDataFolder().getPath(), "scripts");
        this.projectsDirectory = Paths.get(plugin.getDataFolder().getPath(), "projects");
        this.engine = new JSRuntime();
        this.addonManager = addonManager;
    }

    public boolean load() {
        if(!this.loaded) {
            this.loadScripts();
            this.loadProjects();
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

    private void loadProjects() {
        try {
            Files.createDirectories(this.projectsDirectory);
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        File[] subdirs = this.projectsDirectory.toFile().listFiles(File::isDirectory);
        if(subdirs == null || subdirs.length == 0) {
            return;
        }

        Gson gson = new Gson();
        Map<String, ProjectDescriptor> descriptors = new LinkedHashMap<>();

        for(File dir : subdirs) {
            File jsonFile = new File(dir, "project.json");
            if(!jsonFile.exists()) {
                this.plugin.getLogger().warning("Project '" + dir.getName() + "' has no project.json — skipping");
                continue;
            }
            try {
                String raw = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
                ProjectDescriptor desc = gson.fromJson(raw, ProjectDescriptor.class);
                if(desc.getName() == null || desc.getName().isBlank()) {
                    this.plugin.getLogger().warning("Project '" + dir.getName() + "': project.json missing required 'name' field — skipping");
                    continue;
                }
                descriptors.put(dir.getName(), desc);
            } catch(IOException e) {
                this.plugin.getLogger().warning("Project '" + dir.getName() + "': could not read project.json — " + e.getMessage());
            } catch(JsonSyntaxException e) {
                this.plugin.getLogger().warning("Project '" + dir.getName() + "': invalid JSON in project.json — " + e.getMessage());
            }
        }

        for(Map.Entry<String, ProjectDescriptor> entry : descriptors.entrySet()) {
            for(String dep : entry.getValue().getDependencies()) {
                if(dep.endsWith(".js") && !this.scripts.containsKey(dep)) {
                    this.plugin.getLogger().warning("Project '" + entry.getKey() + "' depends on script '" + dep + "' which is not loaded");
                }
            }
        }

        List<String> ordered = this.topoSort(descriptors);
        for(String folderName : ordered) {
            this.loadProject(folderName, descriptors.get(folderName));
        }
    }

    private List<String> topoSort(Map<String, ProjectDescriptor> descriptors) {
        Map<String, List<String>> dependents = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        for(String folderName : descriptors.keySet()) {
            inDegree.put(folderName, 0);
            dependents.put(folderName, new ArrayList<>());
        }

        for(Map.Entry<String, ProjectDescriptor> entry : descriptors.entrySet()) {
            String folderName = entry.getKey();
            for(String dep : entry.getValue().getDependencies()) {
                if(dep.endsWith(".js")) {
                    continue;
                }
                if(!descriptors.containsKey(dep)) {
                    this.plugin.getLogger().warning("Project '" + folderName + "' depends on unknown project '" + dep + "' — ignoring");
                    continue;
                }
                dependents.get(dep).add(folderName);
                inDegree.merge(folderName, 1, Integer::sum);
            }
        }

        Queue<String> queue = new ArrayDeque<>();
        for(Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if(entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();
        while(!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            for(String dependent : dependents.get(current)) {
                int remaining = inDegree.merge(dependent, -1, Integer::sum);
                if(remaining == 0) {
                    queue.add(dependent);
                }
            }
        }

        if(result.size() != descriptors.size()) {
            Set<String> cyclic = descriptors.keySet().stream()
                    .filter(k -> !result.contains(k))
                    .collect(Collectors.toSet());
            this.plugin.getLogger().severe("Circular dependency detected among projects: " + cyclic + " — these projects will not be loaded");
        }

        return result;
    }

    private boolean loadProject(String folderName, ProjectDescriptor desc) {
        File folder = new File(this.projectsDirectory.toFile(), folderName);
        File mainFile = new File(folder, desc.getMain());
        if(!mainFile.exists()) {
            this.plugin.getLogger().severe("Project '" + folderName + "': main file '" + desc.getMain() + "' not found — skipping");
            return false;
        }

        this.plugin.getLogger().info("Loading project: " + folderName + " ('" + desc.getName() + "')");
        JSContext context = this.engine.createContext();
        JavaObjectRegistry registry = this.addBindingsToContext(context, folderName);
        this.projects.put(folderName, context);
        this.projectRegistries.put(folderName, registry);

        try {
            String source = Files.readString(mainFile.toPath(), StandardCharsets.UTF_8);
            context.eval(source, mainFile.getAbsolutePath(), true);
            return true;
        } catch(Exception e) {
            this.unloadProject(folderName);
            e.printStackTrace();
            return false;
        }
    }

    public JSContext getProject(String folderName) {
        return this.projects.get(folderName);
    }

    public JavaObjectRegistry getProjectRegistry(String folderName) {
        return this.projectRegistries.get(folderName);
    }

    public boolean isProjectLoaded(String folderName) {
        return this.projects.containsKey(folderName);
    }

    public List<String> getProjectNames() {
        return new ArrayList<>(this.projects.keySet());
    }

    public boolean unloadProject(String folderName) {
        return this.unloadProject(folderName, null);
    }

    public boolean unloadProject(String folderName, Pipe pipe) {
        for(Object addon : this.addonManager.getAddons().values()) {
            if(addon instanceof RegisteredManager) {
                RegisteredManager manager = (RegisteredManager) addon;
                manager.unregister(folderName);
            }
        }
        JSContext removed = this.projects.remove(folderName);
        if(removed != null) {
            removed.close();
        }
        JavaObjectRegistry registry = this.projectRegistries.remove(folderName);
        if(registry != null) {
            registry.clear();
        }
        return removed != null;
    }

    public boolean reloadProject(String folderName) {
        return this.reloadProject(folderName, null);
    }

    public boolean reloadProject(String folderName, Pipe pipe) {
        File folder = new File(this.projectsDirectory.toFile(), folderName);
        if(!folder.isDirectory()) {
            return false;
        }
        File jsonFile = new File(folder, "project.json");
        if(!jsonFile.exists()) {
            return false;
        }
        ProjectDescriptor desc;
        try {
            String raw = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
            desc = new Gson().fromJson(raw, ProjectDescriptor.class);
        } catch(IOException | JsonSyntaxException e) {
            e.printStackTrace();
            if(pipe != null) {
                pipe.out(e.getMessage());
            }
            return false;
        }
        this.unloadProject(folderName, pipe);
        return this.loadProject(folderName, desc);
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

    public String getProjectListString() {
        File[] subdirs = this.projectsDirectory.toFile().listFiles(File::isDirectory);
        if(subdirs == null || subdirs.length == 0) {
            return "No projects found";
        }
        StringBuilder builder = new StringBuilder();
        for(File dir : subdirs) {
            if(this.projects.containsKey(dir.getName())) {
                builder.append(ChatColor.GREEN);
            } else {
                builder.append(ChatColor.GRAY);
            }
            builder.append(dir.getName());
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