package com.clubobsidian.obbylang;

public enum ObbyLangPlatform {

    SPIGOT,
    BUNGEECORD,
    VELOCITY,
    DISCORD,
    UNKNOWN;

    public static ObbyLangPlatform getCurrent() {
        return ObbyLang.get().getPlugin().getPlatform();
    }
}