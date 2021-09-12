package com.clubobsidian.obbylang.manager.bungeecord;

import com.clubobsidian.obbylang.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import net.md_5.bungee.api.ProxyServer;

public class BungeeCordCustomEventManager extends CustomEventManager {

    @Override
    public void fire(Object[] args) {
        ProxyServer.getInstance().getPluginManager().callEvent(new ObbyLangCustomEvent(args));
    }
}