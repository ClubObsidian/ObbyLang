package com.clubobsidian.obbylang.discord;

import com.clubobsidian.obbylang.discord.plugin.DiscordObbyLangPlugin;

public class Bootstrap {

    private static DiscordObbyLangPlugin plugin;

    public static void main(String[] args) {
        //System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        //BasicConfigurator.configure();
        plugin = new DiscordObbyLangPlugin();
        plugin.onEnable();
    }

    public static DiscordObbyLangPlugin getPlugin() {
        return plugin;
    }
}