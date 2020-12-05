package com.clubobsidian.obbylang.util;

import org.bukkit.Bukkit;

public final class VersionUtil {

	private VersionUtil() {}
	
	public synchronized static String getVersion() 
	{
		String version = "";
		if(Bukkit.getServer() == null)
		{
			return null;
		}
		String name = Bukkit.getServer().getClass().getPackage().getName();
		version = name.substring(name.lastIndexOf('.') + 1);
		return version;
	}
}