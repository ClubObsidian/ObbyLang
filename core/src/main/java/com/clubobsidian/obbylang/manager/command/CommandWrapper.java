package com.clubobsidian.obbylang.manager.command;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public abstract class CommandWrapper<T> {

    private final Object owner;
    private final String commandName;
    private final ScriptObjectMirror base;

    public CommandWrapper(Object owner, String commandName, ScriptObjectMirror base) {
        this.owner = owner;
        this.commandName = commandName;
        this.base = base;
    }

    public Object getOwner() {
        return this.owner;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public ScriptObjectMirror getBase() {
        return this.base;
    }

    public abstract T getCommand();
}