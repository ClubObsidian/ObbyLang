package com.clubobsidian.obbylang.manager.script;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class ScriptWrapper {

    private final ScriptObjectMirror obj;
    private final Object owner;

    public ScriptWrapper(ScriptObjectMirror obj, Object owner) {
        this.obj = obj;
        this.owner = owner;
    }

    public ScriptObjectMirror getScript() {
        return this.obj;
    }

    public Object getOwner() {
        return this.owner;
    }
}