package com.clubobsidian.obbylang.manager.bukkit.command;

import org.bukkit.command.Command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class BukkitCommandWrapper extends CommandWrapper<Command> {

	public BukkitCommandWrapper(Object owner, String commandName, ScriptObjectMirror base) 
	{
		super(owner, commandName, base);
	}

	@Override
	public Command getCommand() 
	{
		return new BukkitCommand(this.getOwner(), this.getCommandName(), this.getBase());
	}
}