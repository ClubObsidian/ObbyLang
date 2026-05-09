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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptManager {

    private static final Gson GSON = new Gson();

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
                .toList();

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
        File[] subDirs = this.projectsDirectory.toFile().listFiles(File::isDirectory);
        if(subDirs == null || subDirs.length == 0) {
            return;
        }
        Map<String, ProjectDescriptor> descriptors = new LinkedHashMap<>();
        Map<String, File> folders = new LinkedHashMap<>();
        for(File dir : subDirs) {
            ProjectDescriptor desc = this.readDescriptor(dir);
            if(desc == null) {
                this.plugin.getLogger().warning("Project in folder '" + dir.getName() + "': missing or invalid project.json — skipping");
                continue;
            }
            if(desc.getName() == null || desc.getName().isBlank()) {
                this.plugin.getLogger().warning("Project in folder '" + dir.getName() + "': project.json missing required 'name' field — skipping");
                continue;
            }
            String key = desc.getName().toLowerCase();
            descriptors.put(key, desc);
            folders.put(key, dir);
        }
        for(Map.Entry<String, ProjectDescriptor> entry : descriptors.entrySet()) {
            for(String dep : entry.getValue().getDependencies()) {
                if(dep.endsWith(".js") && !this.scripts.containsKey(dep)) {
                    this.plugin.getLogger().warning("Project '" + entry.getKey() + "' depends on script '" + dep + "' which is not loaded");
                }
            }
        }
        List<String> ordered = this.topoSort(descriptors);
        for(String key : ordered) {
            this.loadProject(key, descriptors.get(key), folders.get(key));
        }
    }

    private List<String> topoSort(Map<String, ProjectDescriptor> descriptors) {
        Map<String, List<String>> dependents = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for(String key : descriptors.keySet()) {
            inDegree.put(key, 0);
            dependents.put(key, new ArrayList<>());
        }
        for(Map.Entry<String, ProjectDescriptor> entry : descriptors.entrySet()) {
            String key = entry.getKey();
            for(String dep : entry.getValue().getDependencies()) {
                if(dep.endsWith(".js")) {
                    continue;
                }
                String depKey = dep.toLowerCase();
                if(!descriptors.containsKey(depKey)) {
                    this.plugin.getLogger().warning("Project '" + key + "' depends on unknown project '" + dep + "' — ignoring");
                    continue;
                }
                dependents.get(depKey).add(key);
                inDegree.merge(key, 1, Integer::sum);
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

    private boolean loadProject(String projectName, ProjectDescriptor desc, File folder) {
        File mainFile = new File(folder, desc.getMain());
        if(!mainFile.exists()) {
            this.plugin.getLogger().severe("Project '" + projectName + "': main file '" + desc.getMain() + "' not found — skipping");
            return false;
        }
        this.plugin.getLogger().info("Loading project: " + folder.getName() + " ('" + desc.getName() + "')");
        JSContext context = this.engine.createContext();
        JavaObjectRegistry registry = this.addBindingsToContext(context, projectName);
        this.projects.put(projectName, context);
        this.projectRegistries.put(projectName, registry);
        try {
            String source = Files.readString(mainFile.toPath(), StandardCharsets.UTF_8);
            context.eval(source, mainFile.getAbsolutePath(), true);
            return true;
        } catch(Exception e) {
            this.unloadProject(projectName);
            e.printStackTrace();
            return false;
        }
    }

    private Map.Entry<ProjectDescriptor, File> findProject(String name) {
        File[] subDirs = this.projectsDirectory.toFile().listFiles(File::isDirectory);
        if(subDirs == null) return null;
        for(File dir : subDirs) {
            ProjectDescriptor desc = this.readDescriptor(dir);
            if(desc != null && desc.getName() != null && desc.getName().equalsIgnoreCase(name)) {
                return Map.entry(desc, dir);
            }
        }
        return null;
    }

    public boolean loadProject(String name) {
        return this.loadProject(name, null);
    }

    public boolean loadProject(String name, Pipe pipe) {
        try {
            String key = name.toLowerCase();
            if(this.projects.containsKey(key)) {
                return false;
            }
            Map.Entry<ProjectDescriptor, File> found = this.findProject(name);
            if(found == null) {
                return false;
            }
            return this.loadProject(key, found.getKey(), found.getValue());
        } catch (Exception ex) {
            sendStacktrace(ex, pipe);
            ex.printStackTrace();
        }
        return false;
    }

    public JSContext getProject(String projectName) {
        return this.projects.get(projectName.toLowerCase());
    }

    public JavaObjectRegistry getProjectRegistry(String projectName) {
        return this.projectRegistries.get(projectName.toLowerCase());
    }

    public boolean isProjectLoaded(String projectName) {
        return this.projects.containsKey(projectName.toLowerCase());
    }

    public List<String> getProjectNames() {
        return new ArrayList<>(this.projects.keySet());
    }

    public boolean unloadProject(String projectName) {
        return this.unloadProject(projectName, null);
    }

    public boolean unloadProject(String projectName, Pipe pipe) {
        String key = projectName.toLowerCase();
        for(Object addon : this.addonManager.getAddons().values()) {
            if(addon instanceof RegisteredManager registeredManager) {
                registeredManager.unregister(key);
            }
        }
        JSContext removed = this.projects.remove(key);
        if(removed != null) {
            removed.close();
        }
        JavaObjectRegistry registry = this.projectRegistries.remove(key);
        if(registry != null) {
            registry.clear();
        }
        return removed != null;
    }

    public boolean reloadProject(String projectName) {
        return this.reloadProject(projectName, null);
    }

    public boolean reloadProject(String projectName, Pipe pipe) {
        String key = projectName.toLowerCase();
        Map.Entry<ProjectDescriptor, File> found = this.findProject(projectName);
        if(found == null) {
            return false;
        }
        this.unloadProject(key, pipe);
        return this.loadProject(key, found.getKey(), found.getValue());
    }

    private ProjectDescriptor readDescriptor(File folder) {
        File jsonFile = new File(folder, "project.json");
        if(!jsonFile.exists()) {
            return null;
        }
        try {
            String raw = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
            return GSON.fromJson(raw, ProjectDescriptor.class);
        } catch(IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;
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

    public String getProjectListString() {
        File[] subDirs = this.projectsDirectory.toFile().listFiles(File::isDirectory);
        if(subDirs == null || subDirs.length == 0) {
            return "No projects found";
        }
        StringBuilder builder = new StringBuilder();
        for(File dir : subDirs) {
            ProjectDescriptor desc = this.readDescriptor(dir);
            if(desc == null || desc.getName() == null || desc.getName().isBlank()) {
                continue;
            }
            String key = desc.getName().toLowerCase();
            if(this.projects.containsKey(key)) {
                builder.append(ChatColor.GREEN);
            } else {
                builder.append(ChatColor.GRAY);
            }
            builder.append(key);
            builder.append(ChatColor.WHITE + ", ");
        }
        if(builder.isEmpty()) {
            return "No projects found";
        }
        return builder.substring(0, builder.toString().length() - 2);
    }

    public List<String> getScriptNamesRaw() {
        return new ArrayList<>(this.scripts.keySet());
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
            if(addon instanceof RegisteredManager registeredManager) {
                registeredManager.unregister(className);
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
            this.sendStacktrace(e, pipe);
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
        try {
            if(!location.endsWith(".js")) {
                location = location + ".js";
            }
            File file = new File(this.directory.toFile(), location);
            if(!file.exists()) {
                return false;
            }
            boolean unload = this.unloadScript(file.getName(), pipe);
            if(!unload) {
                return false;
            }
            return this.loadScript(location, pipe);
        } catch (Exception ex) {
            this.sendStacktrace(ex, pipe);
            ex.printStackTrace();
        }
        return false;
    }

    public boolean enabledScript(String location) {
        return this.enableScript(location, null);
    }

    public boolean enableScript(String location, Pipe pipe) {
        try {
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
                    Files.copy(Paths.get(file.toURI()), Paths.get(original.toURI()));
                    file.delete();
                    return this.loadScript(location.replace(".dis", ""));
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            this.sendStacktrace(ex, pipe);
            return false;
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
            } catch(IOException ex) {
                ex.printStackTrace();
                this.sendStacktrace(ex, pipe);
                return false;
            }
            file.delete();
            return true;
        }
    }

    private void sendStacktrace(Exception ex, Pipe pipe) {
        if (pipe == null) {
            return;
        }
        String message = ex.getMessage();
        if(message.contains("<eval>")) {
            pipe.out(message);
        } else {
            String st = ExceptionUtils.getStackTrace(ex);
            Pattern pattern = Pattern.compile("(?<=program\\(<eval>:)(\\d*)(?=\\))");
            Matcher matcher = pattern.matcher(st);
            if(matcher.find()) {
                message += " at line " + matcher.group();
            }
            pipe.out(message);
        }
    }
}