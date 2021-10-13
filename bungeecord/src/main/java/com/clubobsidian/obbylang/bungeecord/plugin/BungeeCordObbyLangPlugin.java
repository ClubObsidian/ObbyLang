package com.clubobsidian.obbylang.bungeecord.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.bungeecord.command.ObbyLangCommand;
import com.clubobsidian.obbylang.guice.PluginInjector;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordCustomEventManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordFakeServerManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordListenerManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordMessageManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordProxyManager;
import com.clubobsidian.obbylang.bungeecord.manager.command.BungeeCordCommandManager;
import com.clubobsidian.obbylang.bungeecord.manager.command.BungeeCordCommandWrapperManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.google.inject.Injector;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCordObbyLangPlugin extends Plugin implements ObbyLangPlugin, Listener {

    public static BungeeCordObbyLangPlugin instance;

    private Injector injector;

    @Override
    public boolean createObbyLangCommand() {
        this.getProxy().getPluginManager().registerCommand(this, new ObbyLangCommand("gobbylang"));
        this.getProxy().getPluginManager().registerCommand(this, new ObbyLangCommand("gol"));
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.BUNGEECORD;
    }

    @Override
    public Object getServer() {
        return ProxyServer.getInstance();
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
                .setProxyManager(new BungeeCordProxyManager())
                .setMessageManager(new BungeeCordMessageManager())
                .setCustomEventManager(new BungeeCordCustomEventManager())
                .setFakeServerManager(new BungeeCordFakeServerManager())
                .setListenerManager(new BungeeCordListenerManager())
                .setCommandManager(new BungeeCordCommandManager())
                .setCommandWrapperManager(new BungeeCordCommandWrapperManager())
                .create();


        this.getLogger().info("About to enable obbylang");
        ObbyLang.get().onEnable();
    }

    @Override
    public void onDisable() {
        ObbyLang.get().onDisable();
    }

    public static BungeeCordObbyLangPlugin get() {
        return instance;
    }
}