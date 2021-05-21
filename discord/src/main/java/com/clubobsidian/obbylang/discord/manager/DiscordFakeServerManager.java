package com.clubobsidian.obbylang.discord.manager;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.discord.plugin.DiscordObbyLangPlugin;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;

public class DiscordFakeServerManager extends FakeServerManager {

    @Override
    public Object getPlugin(String plugin) {
        throw new UnsupportedOperationException("Cannot use getPlugin() on ObbyLangDiscord");
    }

    @Override
    public boolean registerListener(Object obj) {
        DiscordObbyLangPlugin plugin = (DiscordObbyLangPlugin) ObbyLang.get().getPlugin();
        plugin.getJDA().addEventListener(obj);
        return true;
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        return -1;
    }
}