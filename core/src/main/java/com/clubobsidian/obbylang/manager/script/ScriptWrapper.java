package com.clubobsidian.obbylang.manager.script;


import org.graalvm.polyglot.Value;

public class ScriptWrapper {

    private final Value script;
    private final Object owner;

    public ScriptWrapper(Value script, Object owner) {
        this.script = script;
        this.owner = owner;
    }

    public Value getScript() {
        return this.script;
    }

    public Object getOwner() {
        return this.owner;
    }
}