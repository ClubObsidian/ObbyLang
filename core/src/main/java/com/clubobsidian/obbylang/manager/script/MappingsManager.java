package com.clubobsidian.obbylang.manager.script;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.util.ClassUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class MappingsManager {

    private static MappingsManager instance;

    public static MappingsManager get() {
        if(instance == null) {
            instance = new MappingsManager();
        }
        return instance;
    }

    private boolean mappingsLoaded = false;
    private final Map<String, String> eventMappings = new ConcurrentHashMap<>();

    private MappingsManager() { }

    public Map<String, String> getEventMappings() {
        return this.eventMappings;
    }

    public boolean addEventMapping(String eventClassPath, String className) {
        if(!ClassUtil.classExists(eventClassPath)) {
            String toLog = "Invalid event class mapping" + eventClassPath;
            ObbyLang.get().getPlugin().getLogger().log(Level.SEVERE, toLog);
            return false;
        }

        this.eventMappings.put(eventClassPath, className.toLowerCase());
        return true;
    }

    public boolean loadEventMappingsFromFile() {
        if(!this.mappingsLoaded) {
            BufferedReader reader = null;
            File mappingsFile = new File(ObbyLang.get().getPlugin().getDataFolder().getPath(), "events.csv");
            if(!mappingsFile.exists())
                return false;

            try {

                reader = new BufferedReader(new FileReader(mappingsFile));
                String line = null;
                while((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    this.eventMappings.put(split[0], split[1]);
                }
            } catch(IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    reader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            this.mappingsLoaded = true;
            return true;
        }
        return false;
    }

    public boolean getMappingsLoaded() {
        return this.mappingsLoaded;
    }
}