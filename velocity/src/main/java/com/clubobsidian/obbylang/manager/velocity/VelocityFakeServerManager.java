package com.clubobsidian.obbylang.manager.velocity;

import java.util.concurrent.TimeUnit;

import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.VelocityObbyLangPlugin;

public class VelocityFakeServerManager extends FakeServerManager {

	@Override
	public Object getPlugin(String plugin) {
		return VelocityObbyLangPlugin
				.get()
				.getServer()
				.getPluginManager()
				.getPlugin(plugin);
	}

	@Override
	public boolean registerListener(Object listener) {
		VelocityObbyLangPlugin plugin = VelocityObbyLangPlugin.get();
		plugin.getServer().getEventManager().register(plugin, listener);
		return true;
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
		VelocityObbyLangPlugin
		.get()
		.getServer()
		.getScheduler()
		.buildTask(VelocityObbyLangPlugin.get(), task)
		.delay(delay, TimeUnit.MILLISECONDS)
		.repeat(period, TimeUnit.MILLISECONDS)
		.schedule();
		//TODO - Implement ids - just return -1 for now
		return -1;
	}
}