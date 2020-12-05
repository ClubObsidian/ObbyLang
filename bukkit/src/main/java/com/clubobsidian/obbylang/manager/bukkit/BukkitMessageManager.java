package com.clubobsidian.obbylang.manager.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.clubobsidian.obbylang.manager.message.MessageManager;

public class BukkitMessageManager extends MessageManager<CommandSender> {

	@Override
	public void broadcast(String message) 
	{
		Bukkit.getServer().broadcastMessage(this.color(message));
	}

	@Override
	public void message(CommandSender sender, String message) 
	{
		sender.sendMessage(this.color(message));
	}

	@Override
	public void msg(CommandSender sender, String message)
	{
		sender.sendMessage(this.color(message));
	}
}