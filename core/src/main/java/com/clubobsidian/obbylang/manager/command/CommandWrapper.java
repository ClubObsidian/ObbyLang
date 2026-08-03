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

package com.clubobsidian.obbylang.manager.command;

import com.caoccao.qjs4j.core.JSFunction;

public abstract class CommandWrapper<T> {

    private final String owner;
    private final String commandName;
    private final JSFunction base;

    public CommandWrapper(String declaringClass, String commandName, JSFunction base) {
        this.owner = declaringClass;
        this.commandName = commandName;
        this.base = base;
    }

    public String getDeclaringClass() {
        return this.owner;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public JSFunction getBase() {
        return this.base;
    }

    public abstract T getCommand();
}