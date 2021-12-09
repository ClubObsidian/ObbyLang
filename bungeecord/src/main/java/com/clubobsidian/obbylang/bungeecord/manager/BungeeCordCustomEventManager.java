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

import com.clubobsidian.obbylang.bungeecord.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import net.md_5.bungee.api.ProxyServer;

import javax.inject.Inject;

public class BungeeCordCustomEventManager extends CustomEventManager {

    @Inject
    protected BungeeCordCustomEventManager(MappingsManager mappingsManager) {
        super(mappingsManager, ObbyLangCustomEvent.class.getName());
    }

    @Override
    public void fire(Object[] args) {
        ProxyServer.getInstance().getPluginManager().callEvent(new ObbyLangCustomEvent(args));
    }
}