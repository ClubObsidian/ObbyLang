package com.clubobsidian.obbylang.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.command.ObbyLangCommand;
import com.clubobsidian.obbylang.command.ObbyLangCommandTabCompleter;
import com.clubobsidian.obbylang.guice.PluginInjector;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitCustomEventManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitDependencyManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitFakeServerManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitListenerManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitMessageManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitProxyManager;
import com.clubobsidian.obbylang.manager.bukkit.command.BukkitCommandManager;
import com.clubobsidian.obbylang.manager.bukkit.command.BukkitCommandWrapperManager;
import com.clubobsidian.obbylang.manager.bukkit.world.InstanceManager;
import com.clubobsidian.obbylang.manager.plugin.PluginManager;
import com.google.inject.Injector;
import javassist.ClassClassPath;
import javassist.ClassPool;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitObbyLangPlugin extends JavaPlugin implements ObbyLangPlugin, Listener {

    private static BukkitObbyLangPlugin instance;

    private Injector injector;

    @Override
    public boolean createObbyLangCommand() {
        this.getCommand("obbylang").setExecutor(new ObbyLangCommand());
        this.getCommand("obbylang").setTabCompleter(new ObbyLangCommandTabCompleter());
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.SPIGOT;
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("Injecting obbylang plugin");
        this.injector = new PluginInjector()
                .injectPlugin(this)
                .setProxyManager(new BukkitProxyManager())
                .setMessageManager(new BukkitMessageManager())
                .setCustomEventManager(new BukkitCustomEventManager())
                .setDependencyManager(new BukkitDependencyManager())
                .setFakeServerManager(new BukkitFakeServerManager())
                .setListenerManager(new BukkitListenerManager())
                .setCommandManager(new BukkitCommandManager())
                .setCommandWrapperManager(new BukkitCommandWrapperManager())
                .create();

        PluginManager.get(); //Initialize plugin manager
        AddonManager.get().registerAddon("instanceManager", InstanceManager.get());

        ClassPool.getDefault().insertClassPath(new ClassClassPath(Listener.class));
        this.getLogger().info("About to enable obbylang");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        ObbyLang.get().onEnable();
    }

    @Override
    public void onDisable() {
        ObbyLang.get().onDisable();
    }

    public static BukkitObbyLangPlugin get() {
        return instance;
    }
}