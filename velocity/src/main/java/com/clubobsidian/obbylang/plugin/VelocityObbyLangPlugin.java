package com.clubobsidian.obbylang.plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.command.ObbyLangCommand;
import com.clubobsidian.obbylang.guice.PluginInjector;
import com.clubobsidian.obbylang.manager.velocity.VelocityCustomEventManager;
import com.clubobsidian.obbylang.manager.velocity.VelocityFakeServerManager;
import com.clubobsidian.obbylang.manager.velocity.VelocityListenerManager;
import com.clubobsidian.obbylang.manager.velocity.VelocityMessageManager;
import com.clubobsidian.obbylang.manager.velocity.VelocityProxyManager;
import com.clubobsidian.obbylang.manager.velocity.command.VelocityCommandManager;
import com.clubobsidian.obbylang.manager.velocity.command.VelocityCommandWrapperManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;


@Plugin(id = "obbylangvelocity", name = "ObbyLangVelocity", version = "${pluginVersion}")
public class VelocityObbyLangPlugin implements ObbyLangPlugin {

	public static VelocityObbyLangPlugin instance;
	
	private Injector injector;
	private ProxyServer server;
	private Logger logger;
	
	private Path pathDataFolder;
	
	@Inject
	public VelocityObbyLangPlugin(ProxyServer server, Logger logger, @DataDirectory Path pathDataFolder) {
		this.server = server;
		this.logger = logger;
		this.pathDataFolder = pathDataFolder;
	}
	
	@Override
	public boolean createObbyLangCommand() {
		this.server.getCommandManager().register(new ObbyLangCommand(), "gobbylang", "gol");
		return true;
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