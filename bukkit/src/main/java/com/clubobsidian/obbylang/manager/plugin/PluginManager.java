package com.clubobsidian.obbylang.manager.plugin;

import org.bukkit.Bukkit;

import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.plugin.dynamicgui.GuiManager;
import com.clubobsidian.obbylang.manager.plugin.protocollib.ProtocolLibManager;

public class PluginManager {

	private static PluginManager instance;
	
	public static PluginManager get()
	{
		if(instance == null)
		{
			instance = new PluginManager();
		}
		return instance;
	}
	
	private boolean dynamicGuiEnabled;
	private boolean protocolLibEnabled;
	private PluginManager() 
	{
		this.dynamicGuiEnabled = this.resolveDynamicGui();
		this.protocolLibEnabled = this.resolveProtocolLib();
		this.addPluginContext();
	}
	
	public boolean isDynamicGuiEnabled()
	{
		return this.dynamicGuiEnabled;
	}
	
	public boolean isProtocolLibEnabled()
	{
		return this.protocolLibEnabled;
	}
	
	private boolean resolveDynamicGui()
	{
		return Bukkit.getServer().getPluginManager().getPlugin("DynamicGui") != null;
	}
	
	private boolean resolveProtocolLib()
	{
		return Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null;
	}
	
	public void addPluginContext()
	{
		if(this.dynamicGuiEnabled)
		{
			AddonManager.get().registerAddon("guiManager", GuiManager.get());
		}
		if(this.protocolLibEnabled)
		{
			AddonManager.get().registerAddon("protocolLib", ProtocolLibManager.get());
		}
	}
}