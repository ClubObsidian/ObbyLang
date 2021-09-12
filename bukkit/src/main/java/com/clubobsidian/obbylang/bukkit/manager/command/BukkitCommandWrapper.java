package com.clubobsidian.obbylang.bukkit.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.command.Command;

public class BukkitCommandWrapper extends CommandWrapper<Command> {

    public BukkitCommandWrapper(Object owner, String commandName, ScriptObjectMirror base) {
        super(owner, commandName, base);
    }

    @Override
    public Command getCommand() {
        return new BukkitCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }
}