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

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public abstract class CommandWrapper<T> {

    private final Object owner;
    private final String commandName;
    private final ScriptObjectMirror base;

    public CommandWrapper(Object owner, String commandName, ScriptObjectMirror base) {
        this.owner = owner;
        this.commandName = commandName;
        this.base = base;
    }

    public Object getOwner() {
        return this.owner;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public ScriptObjectMirror getBase() {
        return this.base;
    }

    public abstract T getCommand();
}