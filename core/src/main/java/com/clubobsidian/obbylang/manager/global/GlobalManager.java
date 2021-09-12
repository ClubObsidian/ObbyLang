package com.clubobsidian.obbylang.manager.global;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalManager {

    private static GlobalManager instance;

    public static GlobalManager get() {
        if(instance == null) {
            instance = new GlobalManager();
        }
        return instance;
    }

    private final Map<String, Object> globals = new ConcurrentHashMap<>();

    private GlobalManager() { }

    public void set(String name, Object passed) {
        this.globals.put(name, passed);
    }

    public Object get(String name) {
        return this.globals.get(name);
    }

    public void remove(String name) {
        this.globals.remove(name);
    }

    public Map<String, Object> getGlobals() {
        return this.globals;
    }
}