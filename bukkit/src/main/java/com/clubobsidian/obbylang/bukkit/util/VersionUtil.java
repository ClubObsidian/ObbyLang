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

package com.clubobsidian.obbylang.bukkit.util;

import org.bukkit.Bukkit;

public final class VersionUtil {

    private VersionUtil() {
    }

    public synchronized static String getVersion() {
        String version = "";
        if(Bukkit.getServer() == null) {
            return null;
        }
        String name = Bukkit.getServer().getClass().getPackage().getName();
        version = name.substring(name.lastIndexOf('.') + 1);
        return version;
    }
}