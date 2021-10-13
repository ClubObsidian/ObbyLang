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

import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class BukkitFakeServerManager extends FakeServerManager {

    @Override
    public Object getPlugin(String plugin) {
        return Bukkit.getServer().getPluginManager().getPlugin(plugin);
    }

    @Override
    public boolean registerListener(Object obj) {
        if(obj instanceof Listener) {
            Listener listener = (Listener) obj;
            Bukkit.getServer().getPluginManager().registerEvents(listener, BukkitObbyLangPlugin.get());
            return true;
        }
        return false;
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        return Bukkit.getServer()
                .getScheduler()
                .scheduleSyncRepeatingTask(BukkitObbyLangPlugin.get(), task, delay, period);
    }
}