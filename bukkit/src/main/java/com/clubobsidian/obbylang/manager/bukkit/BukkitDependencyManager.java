package com.clubobsidian.obbylang.manager.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.plugin.BukkitObbyLangPlugin;

public class BukkitDependencyManager extends DependencyManager implements Listener {

	@Override
	public void registerPluginEnableListener() 
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, BukkitObbyLangPlugin.get());
	}
	
	@EventHandler
	public void onPluginEnableBukkit(PluginEnableEvent event)
	{
		ObbyLang.get().getEventBus().callEvent(new com.clubobsidian.obbylang.event.PluginEnableEvent(event.getPlugin()));
	}
}