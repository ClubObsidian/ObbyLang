package com.clubobsidian.obbylang.velocity.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.obbylang.velocity.command.ObbyLangCommand;
import com.clubobsidian.obbylang.guice.PluginInjector;
import com.clubobsidian.obbylang.velocity.manager.VelocityCustomEventManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityFakeServerManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityListenerManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityMessageManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityProxyManager;
import com.clubobsidian.obbylang.velocity.manager.command.VelocityCommandManager;
import com.clubobsidian.obbylang.velocity.manager.command.VelocityCommandWrapperManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;


@Plugin(id = "obbylangvelocity", name = "ObbyLangVelocity", version = "${pluginVersion}")
public class VelocityObbyLangPlugin implements ObbyLangPlugin {

    public static VelocityObbyLangPlugin instance;

    private Injector injector;
    private final ProxyServer server;
    private final Logger logger;

    private final Path pathDataFolder;

    @Inject
    public VelocityObbyLangPlugin(ProxyServer server, Logger logger, @DataDirectory Path pathDataFolder) {
        this.server = server;
        this.logger = logger;
        this.pathDataFolder = pathDataFolder;
    }

    @Override
    public boolean createObbyLangCommand() {
        this.server.getCommandManager().register("gobbylang", new ObbyLangCommand(), "gol");
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.VELOCITY;
    }

    @Override
    public ProxyServer getServer() {
        return this.server;
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        instance = this;

        this.getLogger().info("Injecting obbylang plugin");
        this.injector = new PluginInjector()
                .injectPlugin(this)
                .setProxyManager(new VelocityProxyManager())
                .setMessageManager(new VelocityMessageManager())
                .setCustomEventManager(new VelocityCustomEventManager())
                .setFakeServerManager(new VelocityFakeServerManager())
                .setListenerManager(new VelocityListenerManager())
                .setCommandManager(new VelocityCommandManager())
                .setCommandWrapperManager(new VelocityCommandWrapperManager())
                .create();


        this.getLogger().info("About to enable obbylang");
        ObbyLang.get().onEnable();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        ObbyLang.get().onDisable();
    }

    public static VelocityObbyLangPlugin get() {
        return instance;
    }

    @Override
    public File getDataFolder() {
        return this.pathDataFolder.toFile();
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}