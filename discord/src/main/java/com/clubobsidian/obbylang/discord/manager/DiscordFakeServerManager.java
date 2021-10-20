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

package com.clubobsidian.obbylang.discord.manager;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.discord.plugin.DiscordObbyLangPlugin;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;

public class DiscordFakeServerManager extends FakeServerManager {

    private final ObbyLangPlugin plugin;

    protected DiscordFakeServerManager(ObbyLangPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Object getPlugin(String plugin) {
        throw new UnsupportedOperationException("Cannot use getPlugin() on ObbyLangDiscord");
    }

    @Override
    public boolean registerListener(Object obj) {
        DiscordObbyLangPlugin plugin = (DiscordObbyLangPlugin) this.plugin;
        plugin.getJDA().addEventListener(obj);
        return true;
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        return -1;
    }
}