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

package com.clubobsidian.obbylang.bungeecord.manager;

import com.clubobsidian.obbylang.bungeecord.plugin.BungeeCordObbyLangPlugin;
import com.clubobsidian.obbylang.manager.scheduler.SchedulerJob;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;

public class BungeeCordFakeServerManager extends FakeServerManager {

    @Override
    public Object getPlugin(String plugin) {
        return ProxyServer.getInstance().getPluginManager().getPlugin(plugin);
    }

    @Override
    public boolean registerListener(Object obj) {
        if(obj instanceof Listener) {
            Listener listener = (Listener) obj;
            ProxyServer.getInstance().getPluginManager().registerListener(BungeeCordObbyLangPlugin.get(), listener);
            return true;
        }
        return false;
    }

    @Override
    public SchedulerJob sync(Runnable task) {
        throw new UnsupportedOperationException("Cannot use sync tasks on proxy");
    }

    @Override
    public SchedulerJob syncDelayed(Runnable task, long delay) {
        throw new UnsupportedOperationException("Cannot use sync tasks on proxy");
    }

    @Override
    public SchedulerJob scheduleSyncRepeatingTask(Runnable task, long initialTickDelay, long repeatingTickDelay) {
        throw new UnsupportedOperationException("Cannot use sync tasks on proxy");
    }
}