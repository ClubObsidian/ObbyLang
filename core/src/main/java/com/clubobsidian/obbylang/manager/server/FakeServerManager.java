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

package com.clubobsidian.obbylang.manager.server;

import com.clubobsidian.obbylang.manager.scheduler.SchedulerJob;

public abstract class FakeServerManager {

    public abstract Object getPlugin(String pluginName);

    public abstract boolean registerListener(Object listener);

    public abstract boolean supportsSyncScheduler();

    public abstract SchedulerJob sync(Runnable task);

    public abstract SchedulerJob syncDelayed(Runnable task, long delay);

    public abstract SchedulerJob scheduleSyncRepeatingTask(Runnable task,
                                                           long initialTickDelay,
                                                           long repeatingTickDelay);
}