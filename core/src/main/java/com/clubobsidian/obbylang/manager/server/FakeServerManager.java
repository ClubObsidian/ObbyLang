package com.clubobsidian.obbylang.manager.server;

import com.clubobsidian.obbylang.ObbyLang;

public abstract class FakeServerManager {

	private static FakeServerManager instance;
	
	public static FakeServerManager get()
	{
		if(instance == null)
		{
			instance = ObbyLang.get().getPlugin().getInjector().getInstance(FakeServerManager.class);
		}
		return instance;
	}
	
	public abstract Object getPlugin(String pluginName);
	public abstract boolean registerListener(Object listener);
	public abstract int scheduleSyncRepeatingTask(Runnable run, int initialTickDelay, int repeatingTickDelay);
	
}