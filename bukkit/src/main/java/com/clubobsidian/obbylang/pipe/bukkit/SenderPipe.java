package com.clubobsidian.obbylang.pipe.bukkit;

import org.bukkit.command.CommandSender;

import com.clubobsidian.obbylang.pipe.Pipe;

public class SenderPipe implements Pipe {

	private CommandSender sender;
	public SenderPipe(CommandSender sender)
	{
		this.sender = sender;
	}
	
	@Override
	public void out(String msg) 
	{
		this.sender.sendMessage(msg);
	}
}