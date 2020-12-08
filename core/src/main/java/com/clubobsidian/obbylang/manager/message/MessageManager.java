package com.clubobsidian.obbylang.manager.message;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.util.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public abstract class MessageManager<T> {

	private static MessageManager<?> instance;
	
	public static MessageManager<?> get()
	{
		if(instance == null)
		{
			instance = ObbyLang.get().getPlugin().getInjector().getInstance(MessageManager.class);
		}
		return instance;
	}

	private MiniMessage miniMessage;
	private GsonComponentSerializer serializer;
	public MessageManager()
	{
		this.miniMessage = MiniMessage
				.builder()
				.markdown()
				.build();
		this.serializer = GsonComponentSerializer.builder().build();
	}
	
	public abstract void msg(T sender, String msg);
	public abstract void message(T sender, String msg);
	public abstract void messageJson(T sender, String json);
	public abstract void broadcast(String str);
	public abstract void broadcastJson(String json);

	public String parseMiniMessage(String message)
	{
		Component component = this.miniMessage.deserialize(message);
		return this.serializer.serialize(component);
	}

	public void miniMsg(T sender, String msg)
	{
		this.miniMessage(sender, msg);
	}

	public void miniMessage(T sender, String msg)
	{
		String json = this.parseMiniMessage(msg);
		this.messageJson(sender, json);
	}

	public void miniBroadcast(String msg)
	{
		String json = this.parseMiniMessage(msg);
		this.broadcastJson(json);
	}
	
	public String color(String str)
	{
		return ChatColor.translateAlternateColorCodes('&', str);
	}
}