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

package com.clubobsidian.obbylang.velocity.manager;

import com.clubobsidian.obbylang.manager.scheduler.SchedulerJob;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;

import java.util.concurrent.TimeUnit;

public class VelocityFakeServerManager extends FakeServerManager {

    @Override
    public Object getPlugin(String plugin) {
        return VelocityObbyLangPlugin
                .get()
                .getServer()
                .getPluginManager()
                .getPlugin(plugin);
    }

    @Override
    public boolean registerListener(Object listener) {
        VelocityObbyLangPlugin plugin = VelocityObbyLangPlugin.get();
        plugin.getServer().getEventManager().register(plugin, listener);
        return true;
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