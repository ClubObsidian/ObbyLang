package com.clubobsidian.obbylang.discord.manager;

import com.clubobsidian.obbylang.manager.message.MessageManager;

import net.dv8tion.jda.api.entities.MessageChannel;

public class DiscordMessageManager extends MessageManager<MessageChannel> {

	@Override
	public void broadcast(String message) 
	{
		throw new UnsupportedOperationException("Broadcast cannot be used on ObbyLang for Discord.");
	}

	@Override
	public void broadcastJson(String json)
	{
		throw new UnsupportedOperationException("Broadcast cannot be used on ObbyLang for Discord.");
	}

	@Override
	public void message(MessageChannel channel, String message) 
	{
		channel.sendMessage(message);
	}

	@Override
	public void messageJson(MessageChannel sender, String json)
	{
		throw new UnsupportedOperationException("Json messaging is not implemented in ObbyLang for Discord.");
	}

	@Override
	public void msg(MessageChannel channel, String message)
	{
		this.message(channel, message);
	}
}