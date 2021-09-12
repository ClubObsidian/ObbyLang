package com.clubobsidian.obbylang.manager.event;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.script.MappingsManager;

public abstract class CustomEventManager {

    private static CustomEventManager instance;

    public static CustomEventManager get() {
        if(instance == null) {
            instance = ObbyLang.get().getPlugin().getInjector().getInstance(CustomEventManager.class);
        }
        return instance;
    }

    public CustomEventManager() {
        MappingsManager.get().addEventMapping("com.clubobsidian.obbylang.velocity.event.ObbyLangCustomEvent", "obbylangcustomevent");
    }

    public abstract void fire(Object[] args);
}