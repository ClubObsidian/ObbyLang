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

package com.clubobsidian.obbylang.bungeecord.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.bungeecord.command.ObbyLangCommand;
import com.clubobsidian.obbylang.inject.PluginInjector;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordCustomEventManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordFakeServerManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordListenerManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordMessageManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordProxyManager;
import com.clubobsidian.obbylang.bungeecord.manager.command.BungeeCordCommandManager;
import com.clubobsidian.obbylang.bungeecord.manager.command.BungeeCordCommandWrapperManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.google.inject.Injector;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCordObbyLangPlugin extends Plugin implements ObbyLangPlugin, Listener {

    public static BungeeCordObbyLangPlugin instance;

    private Injector injector;

    @Override
    public boolean createObbyLangCommand() {
        this.getProxy().getPluginManager().registerCommand(this, new ObbyLangCommand("gobbylang"));
        this.getProxy().getPluginManager().registerCommand(this, new ObbyLangCommand("gol"));
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.BUNGEECORD;
    }

    @Override
    public Object getServer() {
        return ProxyServer.getInstance();
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("Injecting obbylang plugin");
        this.injector = new PluginInjector()
                .injectPlugin(this)
                .setProxyManager(BungeeCordProxyManager.class)
                .setMessageManager(BungeeCordMessageManager.class)
                .setCustomEventManager(BungeeCordCustomEventManager.class)
                .setFakeServerManager(BungeeCordFakeServerManager.class)
                .setListenerManager(BungeeCordListenerManager.class)
                .setCommandManager(BungeeCordCommandManager.class)
                .setCommandWrapperManager(BungeeCordCommandWrapperManager.class)
                .create();

        this.getLogger().info("About to enable obbylang");
        this.injector.getInstance(ObbyLang.class).onEnable();
    }

    @Override
    public void onDisable() {
        this.injector.getInstance(ObbyLang.class).onDisable();
    }

    public static BungeeCordObbyLangPlugin get() {
        return instance;
    }
}