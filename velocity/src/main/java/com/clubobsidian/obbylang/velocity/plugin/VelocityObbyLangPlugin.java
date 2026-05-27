/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.velocity.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.inject.PluginInjector;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.obbylang.velocity.command.VelocityObbyLangCommand;
import com.clubobsidian.obbylang.velocity.manager.VelocityCustomEventManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityFakeServerManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityListenerManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityMessageManager;
import com.clubobsidian.obbylang.velocity.manager.VelocityProxyManager;
import com.clubobsidian.obbylang.velocity.manager.command.VelocityCommandManager;
import com.clubobsidian.obbylang.velocity.manager.command.VelocityCommandWrapperManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;


@Plugin(id = "obbylangvelocity", name = "ObbyLangVelocity", version = "${pluginVersion}")
public class VelocityObbyLangPlugin implements ObbyLangPlugin {

    public static VelocityObbyLangPlugin instance;

    private ObbyLang obbyLang;
    private final ProxyServer server;
    private final Logger logger;

    private final Path pathDataFolder;

    @Inject
    public VelocityObbyLangPlugin(ProxyServer server, Logger logger, @DataDirectory Path pathDataFolder) {
        this.server = server;
        this.logger = logger;
        this.pathDataFolder = pathDataFolder;
    }

    @Override
    public boolean createObbyLangCommand() {
        this.server.getCommandManager().register("gobbylang",
                this.obbyLang.getInstance(VelocityObbyLangCommand.class),
                "gol");
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.VELOCITY;
    }

    @Override
    public ProxyServer getServer() {
        return this.server;
    }

    @Override
    public ObbyLang getObbyLang() {
        return this.obbyLang;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        instance = this;

        this.getLogger().info("Injecting obbylang plugin");
        this.obbyLang = new PluginInjector()
                .injectPlugin(this)
                .setProxyManager(VelocityProxyManager.class)
                .setMessageManager(VelocityMessageManager.class)
                .setCustomEventManager(VelocityCustomEventManager.class)
                .setFakeServerManager(VelocityFakeServerManager.class)
                .setListenerManager(VelocityListenerManager.class)
                .setCommandManager(VelocityCommandManager.class)
                .setCommandWrapperManager(VelocityCommandWrapperManager.class)
                .addAddon(VelocityObbyLangCommand.class)
                .create();

        this.getLogger().info("About to enable obbylang");
        this.obbyLang.onEnable();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        this.obbyLang.onDisable();
    }

    public static VelocityObbyLangPlugin get() {
        return instance;
    }

    @Override
    public File getDataFolder() {
        return this.pathDataFolder.toFile();
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}