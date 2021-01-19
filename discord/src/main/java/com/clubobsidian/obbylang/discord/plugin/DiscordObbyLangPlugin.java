package com.clubobsidian.obbylang.discord.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.discord.command.DiscordConsoleCommandManager;
import com.clubobsidian.obbylang.discord.command.ObbyLangCommand;
import com.clubobsidian.obbylang.discord.command.StopCommand;
import com.clubobsidian.obbylang.discord.console.ConsoleRunnable;
import com.clubobsidian.obbylang.discord.manager.DiscordFakeServerManager;
import com.clubobsidian.obbylang.discord.manager.DiscordMessageManager;
import com.clubobsidian.obbylang.discord.manager.command.DiscordCommandManager;
import com.clubobsidian.obbylang.discord.manager.command.DiscordCommandWrapperManager;
import com.clubobsidian.obbylang.discord.manager.listener.DiscordListenerManager;
import com.clubobsidian.obbylang.guice.PluginInjector;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.wrappy.Configuration;
import com.google.inject.Injector;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class DiscordObbyLangPlugin implements ObbyLangPlugin {

	private static DiscordObbyLangPlugin instance;
	
	private Injector injector;
	private File dataFolder;
	private JDA jda;
	private DiscordConsoleCommandManager consoleCommandManager;
	private AtomicBoolean isRunning;
	private Thread consoleThread;
	
	@Override
	public boolean createObbyLangCommand() 
	{
		this.consoleCommandManager.registerCommand(new ObbyLangCommand("ol"));
		this.consoleCommandManager.registerCommand(new ObbyLangCommand("obbylang"));
		return true;
	}

	@Override
	public ObbyLangPlatform getPlatform()
	{
		return ObbyLangPlatform.DISCORD;
	}

	@Override
	public Injector getInjector() 
	{
		return this.injector;
	}
	
	public void onEnable()
	{
		instance = this;
		this.dataFolder = new File("ObbyLangDiscord");
		
		File configFile = new File(this.getDataFolder(), "config.yml");
		File eventsFile = new File(this.getDataFolder(), "events.csv");
		if(!this.dataFolder.exists())
		{
			this.dataFolder.mkdirs();
		}
		
		if(!configFile.exists())
		{
			InputStream configStream = this.getClass().getResourceAsStream("/config.yml");
			this.saveResource(configStream, configFile);
		}
		
		if(!eventsFile.exists())
		{
			InputStream eventsStream = this.getClass().getResourceAsStream("/events.csv");
			this.saveResource(eventsStream, eventsFile);
		}
		
		Configuration config = Configuration.load(configFile);
		
		String token = config.getString("token");
		if(token == null)
		{
			this.getLogger().info("Bot token not found, shutting down...");
			return;
		}
		
		
		try 
		{
			this.jda = new JDABuilder(token)
					.setEventManager(new AnnotatedEventManager())
					.build();
		} 
		catch (LoginException e) 
		{
			e.printStackTrace();
			this.getLogger().info("Unable to bootstrap, shutting down...");
			return;
		}
		
		
		this.consoleCommandManager = new DiscordConsoleCommandManager();
		//Load extra console commands
		this.consoleCommandManager.registerCommand(new StopCommand());
		
		//Initialize console
		this.isRunning = new AtomicBoolean(true);
		this.jda.addEventListener(this);
		this.consoleThread = new Thread(new ConsoleRunnable());
		this.consoleThread.start();
		
		AddonManager.get().registerAddon("jda", this.jda);
		
		this.getLogger().info("Injecting ObbyLang plugin");
		DiscordCommandManager commandManager = new DiscordCommandManager();
		
		this.injector = new PluginInjector()
				.injectPlugin(this)
				.setMessageManager(new DiscordMessageManager())
				.setFakeServerManager(new DiscordFakeServerManager())
				.setListenerManager(new DiscordListenerManager())
				.setCommandManager(commandManager)
				.setCommandWrapperManager(new DiscordCommandWrapperManager())
				.create();
		
		//Initialize command manager
		this.jda.addEventListener(commandManager);
		
		this.getLogger().info("About to enable ObbyLang");
		ObbyLang.get().onEnable();
		
		this.getLogger().info("ObbyLangDiscord is now loaded and enabled!");
	}
	
	public void onDisable()
	{
		ObbyLang.get().onDisable();
	}
	
	public static DiscordObbyLangPlugin get()
	{
		return instance;
	}

	public JDA getJDA()
	{
		return this.jda;
	}
	
	public AtomicBoolean isRunning()
	{
		return this.isRunning;
	}

	public DiscordConsoleCommandManager getConsoleCommandManager()
	{
		return this.consoleCommandManager;
	}
	
	public Thread getConsoleThread()
	{
		return this.consoleThread;
	}
	
	@Override
	public File getDataFolder() 
	{
		return this.dataFolder;
	}

	@Override
	public Logger getLogger() 
	{
		return Logger.getLogger("ObbyLang");
	}

	@Override
	public Object getServer() 
	{
		return this.jda;
	}
	
	@SubscribeEvent
	public void onShutdown(ShutdownEvent event)
	{
		this.isRunning.set(false);
	}
	
	private boolean saveResource(InputStream input, File outFile)
	{
		try 
		{
			if(!outFile.exists())
			{
				this.getLogger().info(outFile.getName() + " does not exist, copying to directory");
				Files.copy(input, outFile.toPath());
				input.close();
				return true;
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
}