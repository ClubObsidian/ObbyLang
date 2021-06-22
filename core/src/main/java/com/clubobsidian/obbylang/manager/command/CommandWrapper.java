package com.clubobsidian.obbylang.manager.command;


import org.graalvm.polyglot.Value;

public abstract class CommandWrapper<T> {

    private final Object owner;
    private final String commandName;
    private final Value base;

    public CommandWrapper(Object owner, String commandName, Value base) {
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

    public Value getBase() {
        return this.base;
    }

    public abstract T getCommand();
}