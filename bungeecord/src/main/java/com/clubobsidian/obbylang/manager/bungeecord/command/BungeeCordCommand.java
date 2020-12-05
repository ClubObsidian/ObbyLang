package com.clubobsidian.obbylang.manager.bungeecord.command;

import java.util.HashMap;
import java.util.Map;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCordCommand extends Command {

	private Object owner;
	private String command;
	private ScriptObjectMirror base;

	public BungeeCordCommand(Object owner, String command, ScriptObjectMirror base)
	{
		super(command);
		this.owner = owner;
		this.command = command;
		this.base = base;
	}

	@Override
	public void execute(CommandSender sender, String[] args) 
	{
		SenderWrapper<?> wrapper = new BungeeCordSenderWrapper(sender);
		Map<String,Object> properties = new HashMap<>();
		properties.put("args", args);
		properties.put("sender", wrapper);

		this.base.call(this.owner, wrapper, this, this.command, args);
	}
}