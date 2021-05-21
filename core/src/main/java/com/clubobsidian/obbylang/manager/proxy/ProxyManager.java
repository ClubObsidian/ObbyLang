package com.clubobsidian.obbylang.manager.proxy;

import com.clubobsidian.obbylang.ObbyLang;

public abstract class ProxyManager<T> {

    private static ProxyManager<?> instance;

    public static ProxyManager<?> get() {
        if(instance == null) {
            instance = ObbyLang.get().getPlugin().getInjector().getInstance(ProxyManager.class);
        }
        return instance;
    }

    public abstract void send(T player, String server);
}
