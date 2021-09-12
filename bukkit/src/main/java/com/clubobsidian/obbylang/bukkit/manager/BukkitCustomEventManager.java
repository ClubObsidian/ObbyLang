package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.bukkit.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import org.bukkit.Bukkit;

public class BukkitCustomEventManager extends CustomEventManager {

    @Override
    public void fire(Object[] args) {
        Bukkit.getServer().getPluginManager().callEvent(new ObbyLangCustomEvent(args));
    }
}