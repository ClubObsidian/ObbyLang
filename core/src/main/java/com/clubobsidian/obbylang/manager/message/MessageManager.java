package com.clubobsidian.obbylang.manager.message;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.util.ChatColor;

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
	
	public abstract void msg(T sender, String msg);
	
	public abstract void message(T sender, String msg);
	
	public abstract void broadcast(String str);
	
	public String color(String str)
	{
		return ChatColor.translateAlternateColorCodes('&', str);
	}
}