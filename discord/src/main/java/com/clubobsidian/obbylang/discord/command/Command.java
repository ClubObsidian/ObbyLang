package com.clubobsidian.obbylang.discord.command;

import com.clubobsidian.obbylang.pipe.Pipe;

public abstract class Command {

    private String name;

    public Command(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract boolean onCommand(Pipe pipe, String[] args);

}