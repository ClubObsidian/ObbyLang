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

import com.clubobsidian.obbylang.manager.addon.AddonManager;
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
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ObbyLang {

    private final Injector injector;
    private final ObbyLangPlugin plugin;
    private final ScriptManager scriptManager;

    @Inject
    private ObbyLang(Injector injector, ObbyLangPlugin plugin) {
        this.injector = injector;
        this.plugin = plugin;
        this.scriptManager = this.injector.getInstance(ScriptManager.class);
    }

    public <T> T getInstance(Class<? extends T> clazz) {
        return this.injector.getInstance(clazz);
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

        this.injector.getInstance(MappingsManager.class).loadEventMappingsFromFile();
        this.loadBuiltinManagers();
        scriptManager.load();
        this.plugin.createObbyLangCommand();
    }

    public void onDisable() {
        for(String script : this.scriptManager.getScriptNamesRaw()) {
            this.scriptManager.unloadScript(script);
        }
    }

    private void loadBuiltinManagers() {
        AddonManager addonManager = this.getInstance(AddonManager.class);
        addonManager.registerAddon("disable", this.getInstance(DisableManager.class));
        addonManager.registerAddon("scheduler", this.getInstance(SchedulerManager.class));
        addonManager.registerAddon("server", this.plugin.getServer());
        addonManager.registerAddon("listener", this.getInstance(ListenerManager.class));
        addonManager.registerAddon("log", this.plugin.getLogger());
        addonManager.registerAddon("command", this.getInstance(CommandManager.class));
        addonManager.registerAddon("database", this.getInstance(DatabaseManager.class));
        addonManager.registerAddon("messageManager", this.getInstance(MessageManager.class));
        addonManager.registerAddon("global", this.getInstance(GlobalManager.class));
        addonManager.registerAddon("redis", this.getInstance(RedisManager.class));
        addonManager.registerAddon("customEvent", this.getInstance(CustomEventManager.class));
        addonManager.registerAddon("configuration", this.getInstance(ConfigurationManager.class));
        addonManager.registerAddon("proxy", this.getInstance(ProxyManager.class));
        addonManager.registerAddon("mappingsManager", this.getInstance(MappingsManager.class));
        addonManager.registerAddon("dependencyManager", this.getInstance(DependencyManager.class));
        addonManager.registerAddon("scriptManager", this.getInstance(ScriptManager.class));
        addonManager.registerAddon("ObbyLangPlugin", this.plugin);
        addonManager.registerAddon("ObbyLangPlatform", new ObbyLangPlatform.PlatformWrapper(this.plugin));
    }
}