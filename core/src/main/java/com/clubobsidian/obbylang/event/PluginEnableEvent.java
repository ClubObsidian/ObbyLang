package com.clubobsidian.obbylang.event;

import com.clubobsidian.trident.Event;

public class PluginEnableEvent extends Event {

	private Object plugin;
	public PluginEnableEvent(Object plugin)
	{
		this.plugin = plugin;
	}
	
	public Object getPlugin()
	{
		return this.plugin;
	}
}