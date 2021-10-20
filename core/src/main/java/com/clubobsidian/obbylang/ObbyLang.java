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

package com.clubobsidian.obbylang;

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.config.ConfigurationManager;
import com.clubobsidian.obbylang.manager.database.DatabaseManager;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.global.GlobalManager;
import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.clubobsidian.obbylang.manager.message.MessageManager;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import com.clubobsidian.obbylang.manager.redis.RedisManager;
import com.clubobsidian.obbylang.manager.scheduler.SchedulerManager;
import com.clubobsidian.obbylang.manager.script.DisableManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.trident.EventBus;
import com.clubobsidian.trident.eventbus.methodhandle.MethodHandleEventBus;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ObbyLang {

    private final ObbyLangPlugin plugin;

    @Inject
    private ObbyLang(ObbyLangPlugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        System.setProperty("nashorn.args", "--language=es6");
        File dataFolder = this.plugin.getDataFolder();
        this.plugin.getLogger().info("Datafolder:" + dataFolder.getPath());
        if(!dataFolder.exists()) {
            this.plugin.getLogger().info("Data folder does not exist, creating!");
            dataFolder.mkdirs();
        }

        InputStream eventsStream = this.getClass().getResourceAsStream("/events.csv");
        this.plugin.getLogger().info("EventStream:" + (eventsStream == null));
        File eventsFile = new File(dataFolder, "events.csv");
        try {
            if(!eventsFile.exists()) {
                this.plugin.getLogger().info("EventsFile does not exist, copying to directory");
                Files.copy(eventsStream, eventsFile.toPath());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        DependencyManager.get();
        ConfigurationManager.get();
        MappingsManager.get().loadEventMappingsFromFile();
        CustomEventManager.get();
        this.loadBuiltinManagers();
        ScriptManager.get().load();
        this.getPlugin().createObbyLangCommand();
    }

    public void onDisable() {
        for(String script : ScriptManager.get().getScriptNamesRaw()) {
            ScriptManager.get().unloadScript(script);
        }
    }

    private void loadBuiltinManagers() {
        this.addonManager.registerAddon("disable", DisableManager.get());
        this.addonManager.registerAddon("scheduler", SchedulerManager.get());
        this.addonManager.registerAddon("server", ObbyLang.get().getPlugin().getServer());
        this.addonManager.registerAddon("listener", ListenerManager.get());
        this.addonManager.registerAddon("log", this.plugin.getLogger());
        this.addonManager.registerAddon("command", CommandManager.get());
        this.addonManager.registerAddon("database", DatabaseManager.get());
        this.addonManager.registerAddon("messageManager", MessageManager.get());
        this.addonManager.registerAddon("global", GlobalManager.get());
        this.addonManager.registerAddon("redis", RedisManager.get());
        this.addonManager.registerAddon("customEvent", CustomEventManager.get());
        this.addonManager.registerAddon("configuration", ConfigurationManager.get());
        this.addonManager.registerAddon("proxy", ProxyManager.get());
        this.addonManager.registerAddon("mappingsManager", MappingsManager.get());
        this.addonManager.registerAddon("dependencyManager", DependencyManager.get());
        this.addonManager.registerAddon("scriptManager", ScriptManager.get());
        this.addonManager.registerAddon("ObbyLangPlugin", ObbyLang.get().getPlugin());
        this.addonManager.registerAddon("ObbyLangPlatform", ObbyLangPlatform.class);
    }
}