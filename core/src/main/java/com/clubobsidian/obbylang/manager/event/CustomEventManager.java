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

package com.clubobsidian.obbylang.manager.event;

import com.clubobsidian.obbylang.manager.script.MappingsManager;

import javax.inject.Inject;

public abstract class CustomEventManager {

    @Inject
    protected CustomEventManager(MappingsManager mappingsManager, String eventClassName) {
        mappingsManager.addEventMapping(eventClassName, "obbylangcustomevent");
    }

    public abstract void fire(Object[] args);
}