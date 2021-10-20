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

package com.clubobsidian.obbylang;

import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;

public enum ObbyLangPlatform {

    SPIGOT,
    BUNGEECORD,
    VELOCITY,
    DISCORD,
    UNKNOWN;

    public static class PlatformWrapper {

        private final ObbyLangPlugin plugin;

        public PlatformWrapper(ObbyLangPlugin plugin) {
            this.plugin = plugin;
        }

        public ObbyLangPlatform getCurrent() {
            return this.plugin.getPlatform();
        }
    }
}