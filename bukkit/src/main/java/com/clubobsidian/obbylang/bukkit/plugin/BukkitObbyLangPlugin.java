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

package com.clubobsidian.obbylang.bukkit.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.bukkit.command.BukkitObbyLangCommand;
import com.clubobsidian.obbylang.bukkit.command.BukkitObbyLangCommandTabCompleter;
import com.clubobsidian.obbylang.bukkit.manager.BukkitCustomEventManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitDependencyManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitFakeServerManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitListenerManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitMessageManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitProxyManager;
import com.clubobsidian.obbylang.bukkit.manager.command.BukkitCommandManager;
import com.clubobsidian.obbylang.bukkit.manager.command.BukkitCommandWrapperManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.PluginManager;
import com.clubobsidian.obbylang.inject.PluginInjector;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import javassist.ClassClassPath;
import javassist.ClassPool;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitObbyLangPlugin extends JavaPlugin implements ObbyLangPlugin, Listener {

    private static BukkitObbyLangPlugin instance;

    private ObbyLang obbyLang;

    @Override
    public boolean createObbyLangCommand() {
        PluginCommand cmd = this.getCommand("obbylang");
        cmd.setExecutor(this.obbyLang.getInstance(BukkitObbyLangCommand.class));
        cmd.setTabCompleter(this.obbyLang.getInstance(BukkitObbyLangCommandTabCompleter.class));
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.SPIGOT;
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
                .setProxyManager(BukkitProxyManager.class)
                .setMessageManager(BukkitMessageManager.class)
                .setCustomEventManager(BukkitCustomEventManager.class)
                .setDependencyManager(BukkitDependencyManager.class)
                .setFakeServerManager(BukkitFakeServerManager.class)
                .setListenerManager(BukkitListenerManager.class)
                .setCommandManager(BukkitCommandManager.class)
                .setCommandWrapperManager(BukkitCommandWrapperManager.class)
                .addAddon(PluginManager.class)
                .addAddon(BukkitObbyLangCommand.class)
                .addAddon(BukkitObbyLangCommandTabCompleter.class)
                .create();

        ClassPool.getDefault().insertClassPath(new ClassClassPath(Listener.class));
        this.getLogger().info("About to enable ObbyLang");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.obbyLang.onEnable();
    }

    @Override
    public void onDisable() {
        this.obbyLang.onDisable();
    }

    public static BukkitObbyLangPlugin get() {
        return instance;
    }
}