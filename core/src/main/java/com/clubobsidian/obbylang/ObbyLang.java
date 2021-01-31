package com.clubobsidian.obbylang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.clubobsidian.obbylang.manager.config.ConfigurationManager;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.trident.EventBus;
import com.clubobsidian.trident.eventbus.javassist.JavassistEventBus;
import com.clubobsidian.trident.eventbus.reflection.ReflectionEventBus;
import com.google.inject.Inject;

public class ObbyLang  {
	
	private static ObbyLang instance;
	
	public static ObbyLang get()
	{
		if(instance == null)
		{
			instance = new ObbyLang();
		}
		return instance;
	}
	
	@Inject
	private ObbyLangPlugin plugin;
	private EventBus eventBus;
	private ObbyLang()
	{
		this.eventBus = this.getVersionEventBus();
	}
	
	public void onEnable()
	{
		System.setProperty("nashorn.args", "--language=es6");
		File dataFolder = this.plugin.getDataFolder();
		this.plugin.getLogger().info("Datafolder:" + dataFolder.getPath());
		if(!dataFolder.exists())
		{
			this.plugin.getLogger().info("Data folder does not exist, creating!");
			dataFolder.mkdirs();
		}
		
		InputStream eventsStream = this.getClass().getResourceAsStream("/events.csv");
		this.plugin.getLogger().info("EventStream:" + (eventsStream == null));
		File eventsFile = new File(dataFolder, "events.csv");
		try 
		{
			if(!eventsFile.exists())
			{
				this.plugin.getLogger().info("EventsFile does not exist, copying to directory");
				Files.copy(eventsStream, eventsFile.toPath());
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		instance = this;
		DependencyManager.get();
		ConfigurationManager.get();
		MappingsManager.get().loadEventMappingsFromFile();
		CustomEventManager.get();
		ScriptManager.get().load();
		this.getPlugin().createObbyLangCommand();
	}
	
	public void onDisable()
	{
		for(String script : ScriptManager.get().getScriptNamesRaw())
		{
			ScriptManager.get().unloadScript(script);
		}
	}
	
	public ObbyLangPlugin getPlugin()
	{
		return this.plugin;
	}
	
	public EventBus getEventBus()
	{
		return this.eventBus;
	}

	private EventBus getVersionEventBus()
	{
		String version = System.getProperty("java.version");
		if(version.startsWith("1.8"))
		{
			return new JavassistEventBus();
		}

		return new ReflectionEventBus();
	}
}