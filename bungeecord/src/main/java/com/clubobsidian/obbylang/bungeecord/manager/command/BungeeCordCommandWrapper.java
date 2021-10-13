package com.clubobsidian.obbylang.bungeecord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCordCommandWrapper extends CommandWrapper<Command> {

    public BungeeCordCommandWrapper(Object owner, String commandName, ScriptObjectMirror base) {
        super(owner, commandName, base);
    }

    @Override
    public Command getCommand() {
        return new BungeeCordCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }
}