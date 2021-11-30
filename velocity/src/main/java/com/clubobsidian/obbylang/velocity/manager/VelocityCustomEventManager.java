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


import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.velocity.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;
import com.google.inject.Inject;

public class VelocityCustomEventManager extends CustomEventManager {

    @Inject
    protected VelocityCustomEventManager(MappingsManager mappingsManager) {
        super(mappingsManager);
    }

    @Override
    public void fire(Object[] args) {
        VelocityObbyLangPlugin
                .get()
                .getServer()
                .getEventManager().fire(new ObbyLangCustomEvent(args));
    }
}