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

package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.bukkit.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import org.bukkit.Bukkit;

import javax.inject.Inject;

public class BukkitCustomEventManager extends CustomEventManager {

    @Inject
    protected BukkitCustomEventManager(MappingsManager mappingsManager) {
        super(mappingsManager);
    }

    @Override
    public void fire(Object[] args) {
        Bukkit.getServer().getPluginManager().callEvent(new ObbyLangCustomEvent(args));
    }
}