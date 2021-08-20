package com.clubobsidian.obbylang.manager.bukkit.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import org.bukkit.command.Command;
import org.graalvm.polyglot.Value;

public class BukkitCommandWrapper extends CommandWrapper<Command> {

    public BukkitCommandWrapper(Object owner, String commandName, Value base) {
        super(owner, commandName, base);
    }

    @Override
    public Command getCommand() {
        return new BukkitCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }
}