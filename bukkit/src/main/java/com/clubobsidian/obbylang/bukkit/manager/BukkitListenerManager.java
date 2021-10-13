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

import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BukkitListenerManager extends ListenerManager<EventPriority> {

    @Override
    public String getDefaultPriority() {
        return EventPriority.NORMAL.name();
    }

    @Override
    public Class<?> getHandlerClass() {
        return EventHandler.class;
    }

    @Override
    public Class<?> getListenerClass() {
        return Listener.class;
    }

    @Override
    public Class<?> getEventPriorityClass() {
        return EventPriority.class;
    }

    @Override
    public EventPriority[] getPriorities() {
        return EventPriority.values();
    }

    @Override
    public String getPriorityName() {
        return "priority";
    }
}