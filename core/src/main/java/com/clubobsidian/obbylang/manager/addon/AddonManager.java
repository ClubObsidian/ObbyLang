package com.clubobsidian.obbylang.manager.addon;

import java.util.LinkedHashMap;
import java.util.Map;

public class AddonManager {

	private static AddonManager instance;
	
	public static AddonManager get()
	{
		if(instance == null)
		{
			instance = new AddonManager();
		}
		return instance;
	}
	
	private Map<String,Object> addonContext;
	private AddonManager()
	{
		this.addonContext = new LinkedHashMap<>();
	}
	
	public boolean registerAddon(String name, Object object)
	{
		return this.addonContext.put(name, object) != null;
	}
	
	public Map<String, Object> getAddons()
	{
		return new LinkedHashMap<>(this.addonContext);
	}
}