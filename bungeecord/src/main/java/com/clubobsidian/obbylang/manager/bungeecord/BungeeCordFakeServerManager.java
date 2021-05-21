package com.clubobsidian.obbylang.manager.bungeecord;

import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.BungeeCordObbyLangPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class BungeeCordFakeServerManager extends FakeServerManager {

    @Override
    public Object getPlugin(String plugin) {
        return ProxyServer.getInstance().getPluginManager().getPlugin(plugin);
    }

    @Override
    public boolean registerListener(Object obj) {
        if(obj instanceof Listener) {
            Listener listener = (Listener) obj;
            ProxyServer.getInstance().getPluginManager().registerListener(BungeeCordObbyLangPlugin.get(), listener);
            return true;
        }
        return false;
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        ScheduledTask scheduledTask = ProxyServer
                .getInstance()
                .getScheduler()
                .schedule(BungeeCordObbyLangPlugin.get(), task, delay, period, TimeUnit.MILLISECONDS);
        return scheduledTask.getId();
    }
}