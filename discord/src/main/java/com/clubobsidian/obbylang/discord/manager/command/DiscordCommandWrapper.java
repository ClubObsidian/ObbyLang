package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class DiscordCommandWrapper extends CommandWrapper<DiscordCommand> {

    public DiscordCommandWrapper(Object owner, String commandName, ScriptObjectMirror base) {
        super(owner, commandName, base);
    }

    @Override
    public DiscordCommand getCommand() {
        return new DiscordCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }
}