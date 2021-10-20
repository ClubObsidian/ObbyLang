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

package com.clubobsidian.obbylang.bukkit.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.bukkit.command.ObbyLangCommand;
import com.clubobsidian.obbylang.bukkit.command.ObbyLangCommandTabCompleter;
import com.clubobsidian.obbylang.inject.PluginInjector;
import com.clubobsidian.obbylang.bukkit.manager.BukkitCustomEventManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitDependencyManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitFakeServerManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitListenerManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitMessageManager;
import com.clubobsidian.obbylang.bukkit.manager.BukkitProxyManager;
import com.clubobsidian.obbylang.bukkit.manager.command.BukkitCommandManager;
import com.clubobsidian.obbylang.bukkit.manager.command.BukkitCommandWrapperManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.PluginManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.google.inject.Injector;
import javassist.ClassClassPath;
import javassist.ClassPool;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitObbyLangPlugin extends JavaPlugin implements ObbyLangPlugin, Listener {

    private static BukkitObbyLangPlugin instance;

    private Injector injector;

    @Override
    public boolean createObbyLangCommand() {
        this.getCommand("obbylang").setExecutor(new ObbyLangCommand());
        this.getCommand("obbylang").setTabCompleter(new ObbyLangCommandTabCompleter());
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.SPIGOT;
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
                .setProxyManager(BukkitProxyManager.class)
                .setMessageManager(BukkitMessageManager.class)
                .setCustomEventManager(BukkitCustomEventManager.class)
                .setDependencyManager(BukkitDependencyManager.class)
                .setFakeServerManager(BukkitFakeServerManager.class)
                .setListenerManager(BukkitListenerManager.class)
                .setCommandManager(BukkitCommandManager.class)
                .setCommandWrapperManager(BukkitCommandWrapperManager.class)
                .addAddon(PluginManager.class, PluginManager.class)
                .create();

        ClassPool.getDefault().insertClassPath(new ClassClassPath(Listener.class));
        this.getLogger().info("About to enable ObbyLang");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.injector.getInstance(ObbyLang.class).onEnable();
    }

    @Override
    public void onDisable() {
        this.injector.getInstance(ObbyLang.class).onDisable();
    }

    public static BukkitObbyLangPlugin get() {
        return instance;
    }
}