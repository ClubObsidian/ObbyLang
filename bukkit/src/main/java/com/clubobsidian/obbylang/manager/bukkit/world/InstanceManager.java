package com.clubobsidian.obbylang.manager.bukkit.world;

import com.clubobsidian.obbylang.plugin.BukkitObbyLangPlugin;
import org.apache.commons.io.FileUtils;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {

    private static InstanceManager instance;

    public static InstanceManager get() {
        if(instance == null) {
            instance = new InstanceManager();
        }
        return instance;
    }

    private final Map<String, Instance> loadedInstances;
    private final File permanentInstanceFolder;
    private final File temporaryInstanceFolder;
    private final File instanceTemplateFolder;

    private InstanceManager() {
        this.loadedInstances = new ConcurrentHashMap<>();
        this.permanentInstanceFolder = new File(BukkitObbyLangPlugin.get().getDataFolder(), "permanent-instances");
        this.temporaryInstanceFolder = new File(BukkitObbyLangPlugin.get().getDataFolder(), "temporary-instances");
        this.instanceTemplateFolder = new File(BukkitObbyLangPlugin.get().getDataFolder(), "instance-templates");
        this.updateFolders();
    }

    private void updateFolders() {
        //Permanent instance folder
        if(!this.permanentInstanceFolder.exists()) {
            this.permanentInstanceFolder.mkdirs();
        }

        //Temporary instance folder
        if(this.temporaryInstanceFolder.exists()) {
            try {
                FileUtils.deleteDirectory(this.temporaryInstanceFolder);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        this.temporaryInstanceFolder.mkdirs();

        //Template folder
        if(!this.instanceTemplateFolder.exists()) {
            this.instanceTemplateFolder.mkdirs();
        }
    }

    public File getPermanentInstanceFolder() {
        return this.permanentInstanceFolder;
    }

    public File getTemporaryInstanceFolder() {
        return this.temporaryInstanceFolder;
    }

    public Instance create(String templateName, String instanceName, boolean temporary) {
        if(this.loadedInstances.get(instanceName) != null) {
            return null;
        }

        File templateFile = new File(this.instanceTemplateFolder, templateName);
        if(!templateFile.exists() || !templateFile.isDirectory()) {
            return null;
        }
        try {
            File copyToDirectory = new File(this.permanentInstanceFolder, instanceName);
            if(temporary) {
                copyToDirectory = new File(this.temporaryInstanceFolder, instanceName);
            }

            FileUtils.copyDirectory(templateFile, copyToDirectory);
            World world = WorldManager.get().createWorld(instanceName, temporary);
            if(world == null) {
                return null;
            }

            Instance instance = new Instance(world, instanceName, temporary);
            this.loadedInstances.put(instanceName, instance);
            return instance;
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Instance get(String instanceName) {
        return this.loadedInstances.get(instanceName);
    }

    public boolean remove(Instance instance) {
        return this.remove(instance.getInstanceName());
    }

    public boolean remove(String instanceName) {
        if(instanceName == null)
            return false;

        Instance instance = this.loadedInstances.remove(instanceName);
        if(instance == null) {
            return false;
        }

        WorldManager.get().unloadWorld(instance.getWorld());

        if(instance.getTemporary()) {
            File instanceFile = new File(this.getTemporaryInstanceFolder(), instanceName);
            try {
                FileUtils.deleteDirectory(instanceFile);
                return true;
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}