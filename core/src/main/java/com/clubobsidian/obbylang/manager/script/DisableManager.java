package com.clubobsidian.obbylang.manager.script;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisableManager implements RegisteredManager {

    private static DisableManager instance;

    public static DisableManager get() {
        if(instance == null) {
            instance = new DisableManager();
        }
        return instance;
    }

    private Map<String, List<Value>> disableFunctions;

    private DisableManager() {
        this.disableFunctions = new HashMap<>();
    }

    public void register(String declaringClass, Value script) {
        this.init(declaringClass);
        this.disableFunctions.get(declaringClass).add(script);
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(Value script : this.disableFunctions.get(declaringClass)) {
            script.executeVoid();
        }
        this.disableFunctions.keySet().remove(declaringClass);
    }

    private void init(String declaringClass) {
        if(this.disableFunctions.get(declaringClass) == null) {
            this.disableFunctions.put(declaringClass, new ArrayList<>());
        }
    }

}
