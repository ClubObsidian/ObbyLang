package com.clubobsidian.obbylang.pipe.velocity;

import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.util.MessageUtil;
import com.velocitypowered.api.command.CommandSource;

public class SourcePipe implements Pipe {
	
	private CommandSource sender;
	
	public SourcePipe(CommandSource sender) {
		this.sender = sender;
	}
	
	@Override
	public void out(String message) {
		MessageUtil.sendMessage(this.sender, message);
	}
}