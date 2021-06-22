package com.clubobsidian.obbylang.manager.plugin;

import org.graalvm.polyglot.Value;

public class DependencyWrapper {

    private final Value script;
    private final String[] dependencies;

    public DependencyWrapper(Value script, String[] dependencies) {
        this.script = script;
        this.dependencies = dependencies;
    }

    public Value getScript() {
        return this.script;
    }

    public String[] getDependencies() {
        return this.dependencies;
    }
}