/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.discord.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.discord.command.DiscordConsoleCommandManager;
import com.clubobsidian.obbylang.discord.command.DiscordObbyLangCommand;
import com.clubobsidian.obbylang.discord.command.StopCommand;
import com.clubobsidian.obbylang.discord.console.ConsoleRunnable;
import com.clubobsidian.obbylang.discord.manager.DiscordFakeServerManager;
import com.clubobsidian.obbylang.discord.manager.DiscordMessageManager;
import com.clubobsidian.obbylang.discord.manager.command.DiscordCommandManager;
import com.clubobsidian.obbylang.discord.manager.command.DiscordCommandWrapperManager;
import com.clubobsidian.obbylang.discord.manager.listener.DiscordListenerManager;
import com.clubobsidian.obbylang.inject.PluginInjector;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.wrappy.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class DiscordObbyLangPlugin implements ObbyLangPlugin {

    private static DiscordObbyLangPlugin instance;

    private ObbyLang obbyLang;
    private File dataFolder;
    private JDA jda;
    private DiscordConsoleCommandManager consoleCommandManager;
    private AtomicBoolean isRunning;
    private Thread consoleThread;

    @Override
    public boolean createObbyLangCommand() {
        this.consoleCommandManager.register(this.obbyLang.getInstance(DiscordObbyLangCommand.class), "ol");
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.DISCORD;
    }

    @Override
    public ObbyLang getObbyLang() {
        return this.obbyLang;
    }

    public void onEnable() {
        instance = this;
        this.dataFolder = new File("ObbyLangDiscord");

        File configFile = new File(this.getDataFolder(), "config.yml");
        File eventsFile = new File(this.getDataFolder(), "events.csv");
        if(!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }

        if(!configFile.exists()) {
            InputStream configStream = this.getClass().getResourceAsStream("/config.yml");
            this.saveResource(configStream, configFile);
        }

        if(!eventsFile.exists()) {
            InputStream eventsStream = this.getClass().getResourceAsStream("/events.csv");
            this.saveResource(eventsStream, eventsFile);
        }

        Configuration config = Configuration.load(configFile);

        String token = config.getString("token");
        if(token == null) {
            this.getLogger().info("Bot token not found, shutting down...");
            return;
        }


        try {
            this.jda = JDABuilder.create(token, EnumSet.allOf(GatewayIntent.class))
                    .setEventManager(new AnnotatedEventManager())
                    .build();
        } catch(LoginException e) {
            e.printStackTrace();
            this.getLogger().info("Unable to bootstrap, shutting down...");
            return;
        }


        this.consoleCommandManager = new DiscordConsoleCommandManager();
        //Load extra console commands
        this.consoleCommandManager.register(new StopCommand());

        //Initialize console
        this.isRunning = new AtomicBoolean(true);
        this.jda.addEventListener(this);
        this.consoleThread = new Thread(new ConsoleRunnable());
        this.consoleThread.start();

        this.obbyLang.getInstance(AddonManager.class).registerAddon("jda", this.jda);

        this.getLogger().info("Injecting ObbyLang plugin");

        this.obbyLang = new PluginInjector()
                .injectPlugin(this)
                .setMessageManager(DiscordMessageManager.class)
                .setFakeServerManager(DiscordFakeServerManager.class)
                .setListenerManager(DiscordListenerManager.class)
                .setCommandManager(DiscordCommandManager.class)
                .setCommandWrapperManager(DiscordCommandWrapperManager.class)
                .addAddon(DiscordObbyLangCommand.class)
                .create();

        DiscordCommandManager commandManager = (DiscordCommandManager) this.obbyLang.getInstance(CommandManager.class);
        //Initialize command manager
        this.jda.addEventListener(commandManager);

        this.getLogger().info("About to enable ObbyLang");
        this.obbyLang.onEnable();
        this.getLogger().info("ObbyLangDiscord is now loaded and enabled!");
    }

    public void onDisable() {
        this.obbyLang.onDisable();
    }

    public static DiscordObbyLangPlugin get() {
        return instance;
    }

    public JDA getJDA() {
        return this.jda;
    }

    public AtomicBoolean isRunning() {
        return this.isRunning;
    }

    public DiscordConsoleCommandManager getConsoleCommandManager() {
        return this.consoleCommandManager;
    }

    public Thread getConsoleThread() {
        return this.consoleThread;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public Logger getLogger() {
        return Logger.getLogger("ObbyLang");
    }

    @Override
    public Object getServer() {
        return this.jda;
    }

    @SubscribeEvent
    public void onShutdown(ShutdownEvent event) {
        this.isRunning.set(false);
    }

    private boolean saveResource(InputStream input, File outFile) {
        try {
            if(!outFile.exists()) {
                this.getLogger().info(outFile.getName() + " does not exist, copying to directory");
                Files.copy(input, outFile.toPath());
                input.close();
                return true;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}