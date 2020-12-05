package com.clubobsidian.obbylang.manager.bukkit.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class BukkitTabCompleter implements TabCompleter {

	private Object owner;
	private ScriptObjectMirror script;
	public BukkitTabCompleter(Object owner, ScriptObjectMirror base)
	{
		this.owner = owner;
		this.script = base;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) 
	{
		BukkitSenderWrapper senderWrapper = new BukkitSenderWrapper(sender);
		Object call = script.call(owner, senderWrapper, command, alias, args);
		if(call == null || !(call instanceof List))
		{
			return null;
		}
		
		return (List<String>) call;
	}
}