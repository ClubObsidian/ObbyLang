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

package com.clubobsidian.obbylang.bungeecord.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.bungeecord.command.BungeeCordObbyLangCommand;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordCustomEventManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordFakeServerManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordListenerManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordMessageManager;
import com.clubobsidian.obbylang.bungeecord.manager.BungeeCordProxyManager;
import com.clubobsidian.obbylang.bungeecord.manager.command.BungeeCordCommandManager;
import com.clubobsidian.obbylang.bungeecord.manager.command.BungeeCordCommandWrapperManager;
import com.clubobsidian.obbylang.inject.PluginInjector;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class BungeeCordObbyLangPlugin extends Plugin implements ObbyLangPlugin, Listener {

    public static BungeeCordObbyLangPlugin instance;

    private ObbyLang obbyLang;

    @Override
    public boolean createObbyLangCommand() {
        PluginManager pm = this.getProxy().getPluginManager();
        pm.registerCommand(this, this.obbyLang.getInstance(BungeeCordObbyLangCommand.class));
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
    public ObbyLang getObbyLang() {
        return this.obbyLang;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("Injecting obbylang plugin");
        this.obbyLang = new PluginInjector()
                .injectPlugin(this)
                .setProxyManager(BungeeCordProxyManager.class)
                .setMessageManager(BungeeCordMessageManager.class)
                .setCustomEventManager(BungeeCordCustomEventManager.class)
                .setFakeServerManager(BungeeCordFakeServerManager.class)
                .setListenerManager(BungeeCordListenerManager.class)
                .setCommandManager(BungeeCordCommandManager.class)
                .setCommandWrapperManager(BungeeCordCommandWrapperManager.class)
                .addAddon(BungeeCordObbyLangCommand.class)
                .create();

        this.getLogger().info("About to enable obbylang");
        this.obbyLang.onEnable();
    }

    @Override
    public void onDisable() {
        this.obbyLang.onDisable();
    }

    public static BungeeCordObbyLangPlugin get() {
        return instance;
    }
}