package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import org.graalvm.polyglot.Value;

public class DiscordCommandWrapper extends CommandWrapper<DiscordCommand> {

    public DiscordCommandWrapper(Object owner, String commandName, Value base) {
        super(owner, commandName, base);
    }

    @Override
    public DiscordCommand getCommand() {
        return new DiscordCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }
}