package com.clubobsidian.obbylang.manager.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.BukkitObbyLangPlugin;

public class BukkitFakeServerManager extends FakeServerManager {

	@Override
	public Object getPlugin(String plugin) 
	{
		return Bukkit.getServer().getPluginManager().getPlugin(plugin);
	}

	@Override
	public boolean registerListener(Object obj) 
	{
		if(obj instanceof Listener)
		{
			Listener listener = (Listener) obj;
			Bukkit.getServer().getPluginManager().registerEvents(listener, BukkitObbyLangPlugin.get());
			return true;
		}
		return false;
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) 
	{
		return Bukkit.getServer()
		.getScheduler()
		.scheduleSyncRepeatingTask(BukkitObbyLangPlugin.get(), task, delay, period);
	}
}