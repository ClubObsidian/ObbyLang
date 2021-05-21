package com.clubobsidian.obbylang.manager.global;

import java.util.HashMap;
import java.util.Map;

public class GlobalManager {

    private static GlobalManager instance;

    public static GlobalManager get() {
        if(instance == null) {
            instance = new GlobalManager();
        }
        return instance;
    }

    private Map<String, Object> globals;

    private GlobalManager() {
        this.globals = new HashMap<>();
    }

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