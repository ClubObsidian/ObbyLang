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

package com.clubobsidian.obbylang.velocity.manager.command;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSFunction;
import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.velocitypowered.api.command.Command;

public class VelocityCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, JSFunction script) {
        return new VelocityCommandWrapper(declaringClass, command, script);
    }
}