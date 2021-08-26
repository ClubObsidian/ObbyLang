package com.clubobsidian.obbylang;

import com.clubobsidian.obbylang.loader.JarLoader;
import com.clubobsidian.obbylang.manager.config.ConfigurationManager;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.trident.EventBus;
import com.clubobsidian.trident.eventbus.javassist.JavassistEventBus;
import com.clubobsidian.trident.eventbus.reflection.ReflectionEventBus;
import com.clubobsidian.wrappy.Configuration;
import com.clubobsidian.wrappy.ConfigurationSection;
import com.google.inject.Inject;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ObbyLang {

    private static ObbyLang instance;

    public static ObbyLang get() {
        if(instance == null) {
            instance = new ObbyLang();
        }
        return instance;
    }

    @Inject
    private ObbyLangPlugin plugin;
    private final EventBus eventBus;
    private JarLoader jarLoader;

    private ObbyLang() {
        this.eventBus = this.getVersionEventBus();
    }

    public void onEnable() {
        System.setProperty("nashorn.args", "--language=es6");
        File dataFolder = this.plugin.getDataFolder();
        this.plugin.getLogger().info("Datafolder:" + dataFolder.getPath());
        if(!dataFolder.exists()) {
            this.plugin.getLogger().info("Data folder does not exist, creating!");
            dataFolder.mkdirs();
        }

        InputStream eventsStream = this.getClass().getResourceAsStream("/events.csv");
        this.plugin.getLogger().info("EventStream:" + (eventsStream == null));
        File eventsFile = new File(dataFolder, "events.csv");
        try {
            if(!eventsFile.exists()) {
                this.plugin.getLogger().info("EventsFile does not exist, copying to directory");
                Files.copy(eventsStream, eventsFile.toPath());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        InputStream jarMappingsStream = this.getClass().getResourceAsStream("/jar-mappings.yml");
        this.plugin.getLogger().info("Jar Mappings: " + (jarMappingsStream == null));
        File jarMappingsFile = new File(dataFolder, "jar-mappings.yml");
        try {
            if(!jarMappingsFile.exists()) {
                this.plugin.getLogger().info("JarMappings file does not exist, copying to directory");
                Files.copy(jarMappingsStream, jarMappingsFile.toPath());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        File libraries = new File(dataFolder, "libraries");
        if(!libraries.exists()) {
            libraries.mkdirs();
        }
        File remappedLibraries = new File(dataFolder, "remapped-libraries");
        if(!remappedLibraries.exists()) {
            remappedLibraries.mkdirs();
        }

        ConfigurationSection relocations = Configuration
                .load(jarMappingsFile)
                .getConfigurationSection("relocations");
        List<Relocation> rules = new ArrayList<>();
        for(String key : relocations.getKeys()) {
            rules.add(new Relocation(key, relocations.getString(key)));
        }

        JarLoader loader = new JarLoader();

        for(File file : FileUtils.listFiles(libraries, new String[]{"jar"}, false)) {
            File remappedFile = new File(remappedLibraries, "remapped-" + file.getName());
            if(!remappedFile.exists()) {
                JarRelocator relocator = new JarRelocator(file, remappedFile, rules);
                try {
                    relocator.run();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            if(remappedFile.exists() && loader.addFile(remappedFile)) {
                this.plugin.getLogger().info("Loaded library file: " + remappedFile.getName());
            } else {
                this.plugin.getLogger().info("Unable to load library file: " + remappedFile.getName());
            }
        }


        instance = this;
        DependencyManager.get();
        ConfigurationManager.get();
        MappingsManager.get().loadEventMappingsFromFile();
        CustomEventManager.get();
        ScriptManager.get().load();
        this.getPlugin().createObbyLangCommand();
    }

    public void onDisable() {
        for(String script : ScriptManager.get().getScriptNamesRaw()) {
            ScriptManager.get().unloadScript(script);
        }
    }

    public ObbyLangPlugin getPlugin() {
        return this.plugin;
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }

    public JarLoader getJarLoader() {
        return this.jarLoader;
    }

    private EventBus getVersionEventBus() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.8")) {
            return new JavassistEventBus();
        }

        return new ReflectionEventBus();
    }

}