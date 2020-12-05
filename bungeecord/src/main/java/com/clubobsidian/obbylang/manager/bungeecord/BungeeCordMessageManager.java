package com.clubobsidian.obbylang.manager.bungeecord;

import com.clubobsidian.obbylang.manager.message.MessageManager;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class BungeeCordMessageManager extends MessageManager<CommandSender> {

	@Override
	public void broadcast(String message) 
	{
		ProxyServer.getInstance().broadcast(this.color(message));
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