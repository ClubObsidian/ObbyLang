package com.clubobsidian.obbylang.manager.bukkit;

import org.bukkit.Bukkit;

import com.clubobsidian.obbylang.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;

public class BukkitCustomEventManager extends CustomEventManager {

	@Override
	public void fire(Object[] args) 
	{
		Bukkit.getServer().getPluginManager().callEvent(new ObbyLangCustomEvent(args));
	}
}