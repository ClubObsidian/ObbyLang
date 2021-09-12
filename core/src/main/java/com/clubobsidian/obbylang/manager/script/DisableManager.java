package com.clubobsidian.obbylang.manager.script;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisableManager implements RegisteredManager {

    private static DisableManager instance;

    public static DisableManager get() {
        if(instance == null) {
            instance = new DisableManager();
        }
        return instance;
    }

    private final Map<String, List<ScriptObjectMirror>> disableFunctions = new ConcurrentHashMap<>();

    private DisableManager() { }

    public void register(String declaringClass, ScriptObjectMirror script) {
        this.init(declaringClass);
        this.disableFunctions.get(declaringClass).add(script);
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        CompiledScript owner = ScriptManager.get().getScript(declaringClass);
        for(ScriptObjectMirror script : this.disableFunctions.get(declaringClass)) {
            script.call(owner);
        }
        this.disableFunctions.keySet().remove(declaringClass);
    }

    private void init(String declaringClass) {
        if(this.disableFunctions.get(declaringClass) == null) {
            this.disableFunctions.put(declaringClass, new ArrayList<>());
        }
    }

}
