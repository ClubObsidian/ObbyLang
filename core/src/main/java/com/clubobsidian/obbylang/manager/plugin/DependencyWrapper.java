package com.clubobsidian.obbylang.manager.plugin;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class DependencyWrapper {

    private final ScriptObjectMirror script;
    private final String[] dependencies;

    public DependencyWrapper(ScriptObjectMirror script, String[] dependencies) {
        this.script = script;
        this.dependencies = dependencies;
    }

    public ScriptObjectMirror getScript() {
        return this.script;
    }

    public String[] getDependencies() {
        return this.dependencies;
    }
}