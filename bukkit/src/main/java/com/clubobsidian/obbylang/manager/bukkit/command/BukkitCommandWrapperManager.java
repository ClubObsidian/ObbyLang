package com.clubobsidian.obbylang.manager.bukkit.command;

import org.bukkit.command.Command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class BukkitCommandWrapperManager extends CommandWrapperManager<Command> {

	@Override
	public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script) 
	{
		return new BukkitCommandWrapper(script, command, script);
	}
}