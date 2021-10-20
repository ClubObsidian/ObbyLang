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

package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.trident.EventBus;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class BukkitDependencyManager extends DependencyManager implements Listener {

    protected BukkitDependencyManager(EventBus eventBus, ScriptManager scriptManager) {
        super(eventBus, scriptManager);
    }

    @Override
    public void registerPluginEnableListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, BukkitObbyLangPlugin.get());
    }

    @EventHandler
    public void onPluginEnableBukkit(PluginEnableEvent event) {
        this.getEventBus().callEvent(new com.clubobsidian.obbylang.manager.event.PluginEnableEvent(event.getPlugin()));
    }
}