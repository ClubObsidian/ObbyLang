package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class BukkitDependencyManager extends DependencyManager implements Listener {

    @Override
    public void registerPluginEnableListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, BukkitObbyLangPlugin.get());
    }

    @EventHandler
    public void onPluginEnableBukkit(PluginEnableEvent event) {
        ObbyLang.get().getEventBus().callEvent(new com.clubobsidian.obbylang.manager.event.PluginEnableEvent(event.getPlugin()));
    }
}