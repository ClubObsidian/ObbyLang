package com.clubobsidian.obbylang.manager.bukkit.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class BukkitCommand extends Command implements CommandExecutor {

	private Object owner;
	private String command;
	private ScriptObjectMirror base;
	private TabCompleter tabCompleter;
	public BukkitCommand(Object owner, String command, ScriptObjectMirror base)
	{
		super(command);
		this.owner = owner;
		this.command = command;
		this.base = base;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if(label.equalsIgnoreCase(this.command))
		{
			SenderWrapper<?> wrapper = new BukkitSenderWrapper(sender);

			Object ret = this.base.call(this.owner, wrapper, command, label, args);
			if(ret != null && ret instanceof Boolean)
			{
				return (boolean) ret;
			}
		}
		return false;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) 
	{
		return this.onCommand(sender, this, commandLabel, args);
	}
	
	public void setTabCompleter(TabCompleter tabCompleter)
	{
		this.tabCompleter = tabCompleter;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
	{
		if(this.tabCompleter == null)
		{
			return super.tabComplete(sender, alias, args);
		}
		
		return this.tabCompleter.onTabComplete(sender, this, alias, args);
	}
}