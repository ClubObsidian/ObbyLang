package com.clubobsidian.obbylang.manager.bukkit.world;

import org.bukkit.World;

public class Instance {

	private final World world;
	private final String instanceName;
	private final boolean temporary;
	public Instance(World world, String instanceName, boolean temporary)
	{
		this.world = world;
		this.instanceName = instanceName;
		this.temporary = temporary;
	}
	
	public World getWorld()
	{
		return this.world;
	}
	
	public String getInstanceName()
	{
		return this.instanceName;
	}
	
	public boolean getTemporary()
	{
		return this.temporary;
	}
}