package com.clubobsidian.obbylang.pipe.bungeecord;

import com.clubobsidian.obbylang.pipe.Pipe;

import net.md_5.bungee.api.CommandSender;

public class SenderPipe implements Pipe {
	
	private CommandSender sender;
	public SenderPipe(CommandSender sender)
	{
		this.sender = sender;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void out(String message) 
	{
		this.sender.sendMessage(message);
	}
}