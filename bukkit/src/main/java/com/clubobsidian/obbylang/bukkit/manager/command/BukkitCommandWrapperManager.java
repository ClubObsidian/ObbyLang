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

package com.clubobsidian.obbylang.bukkit.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.command.Command;

public class BukkitCommandWrapperManager extends CommandWrapperManager<Command, String> {

    @Override
    public CommandWrapper<Command, String> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script) {
        return new BukkitCommandWrapper(script, command, script);
    }
}