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

import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;

public class VelocityListenerManager extends ListenerManager<PostOrder> {

    @Inject
    protected VelocityListenerManager(MappingsManager mappingsManager,
                                      ScriptManager scriptManager,
                                      FakeServerManager fakeServer) {
        super(mappingsManager, scriptManager, fakeServer);
    }

    @Override
    public String getDefaultPriority() {
        return "NORMAL";
    }

    @Override
    public Class<?> getHandlerClass() {
        return Subscribe.class;
    }

    @Override
    public Class<?> getListenerClass() {
        return null;
    }

    @Override
    public Class<?> getEventPriorityClass() {
        return PostOrder.class;
    }

    @Override
    public PostOrder[] getPriorities() {
        return PostOrder.values();
    }

    @Override
    public String getPriorityName() {
        return "order";
    }
}