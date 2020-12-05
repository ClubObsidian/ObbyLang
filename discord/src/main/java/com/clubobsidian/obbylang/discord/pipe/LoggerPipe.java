package com.clubobsidian.obbylang.discord.pipe;

import com.clubobsidian.obbylang.discord.plugin.DiscordObbyLangPlugin;
import com.clubobsidian.obbylang.pipe.Pipe;

public class LoggerPipe implements Pipe {

	@Override
	public void out(String msg) 
	{
		DiscordObbyLangPlugin.get().getLogger().info(msg);
	}
}