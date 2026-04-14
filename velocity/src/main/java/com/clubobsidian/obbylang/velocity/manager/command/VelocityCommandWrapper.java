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

import com.caoccao.qjs4j.core.JSFunction;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.velocitypowered.api.command.Command;

public class VelocityCommandWrapper extends CommandWrapper<Command> {

    public VelocityCommandWrapper(String declaringClass, String commandName, JSFunction base) {
        super(declaringClass, commandName, base);
    }

    @Override
    public Command getCommand() {
        return new VelocityCommand(this.getDeclaringClass(), this.getCommandName(), this.getBase());
    }
}