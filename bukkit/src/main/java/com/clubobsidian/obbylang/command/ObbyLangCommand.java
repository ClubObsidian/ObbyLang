package com.clubobsidian.obbylang.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.pipe.bukkit.SenderPipe;

public class ObbyLangCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
			if(sender.hasPermission("obbylang.use"))
			{
				if(args.length == 2)
				{
					Pipe pipe = new SenderPipe(sender);
					if(args[0].equalsIgnoreCase("load"))
					{
						boolean loaded = ScriptManager.get().loadScript(args[1], pipe);
						if(loaded)
						{
							sender.sendMessage("Script has been loaded");
						}
						else
						{
							sender.sendMessage("Script could not be loaded");
						}
					}
					else if(args[0].equalsIgnoreCase("unload"))
					{
						boolean unloaded = ScriptManager.get().unloadScript(args[1], pipe);
						if(unloaded)
						{
							sender.sendMessage("Script has been unloaded");
						}
						else
						{
							sender.sendMessage("Script could not be unloaded");
						}
					}
					else if(args[0].equalsIgnoreCase("reload"))
					{
						boolean reload = ScriptManager.get().reloadScript(args[1], pipe);
						if(reload)
						{
							sender.sendMessage("Script has been reloaded");
						}
						else
						{
							sender.sendMessage("Script could not be reloaded");
							sender.sendMessage("Attemping to load the script");
							boolean load = ScriptManager.get().loadScript(args[1], pipe);
							if(load)
							{
								sender.sendMessage("Script has been loaded");
							}
							else
							{
								sender.sendMessage("Script could not be loaded");
							}
						}
					}
					else if(args[0].equalsIgnoreCase("enable"))
					{
						boolean enable = ScriptManager.get().enableScript(args[1], pipe);
						if(enable)
						{
							sender.sendMessage("Script has been enabled");
						}
						else
						{
							sender.sendMessage("Script can not be enabled");
						}
					}
					else if(args[0].equalsIgnoreCase("disable"))
					{
						boolean disable = ScriptManager.get().disableScript(args[1], pipe);
						if(disable)
						{
							sender.sendMessage("Script has been disabled");
						}
						else
						{
							sender.sendMessage("Script can not be disabled");
						}	
					}
					else
					{
						this.sendCommandList(label, sender);
					}
					return true;
				}
				else if(args.length == 1)
				{
					if(args[0].equalsIgnoreCase("list"))
					{
						sender.sendMessage(ScriptManager.get().getScriptListString());
						return true;
					}
				}
				
				this.sendCommandList(label, sender);
				return true;
				
			}
		return false;
	}

	private void sendCommandList(String label, CommandSender sender)
	{
		sender.sendMessage("/" + label + " load <script>");
		sender.sendMessage("/" + label + " unload <script>");
		sender.sendMessage("/" + label + " reload <script>");
		sender.sendMessage("/" + label + " enable <script>");
		sender.sendMessage("/" + label + " disable <script>");
		sender.sendMessage("/" + label + " list");
	}
}