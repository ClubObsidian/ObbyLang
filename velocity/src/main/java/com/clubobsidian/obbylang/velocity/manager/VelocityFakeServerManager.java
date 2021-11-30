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
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        VelocityObbyLangPlugin
                .get()
                .getServer()
                .getScheduler()
                .buildTask(VelocityObbyLangPlugin.get(), task)
                .delay(delay, TimeUnit.MILLISECONDS)
                .repeat(period, TimeUnit.MILLISECONDS)
                .schedule();
        //TODO - Implement ids - just return -1 for now
        return -1;
    }
}