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

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.google.inject.Inject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommandManager implements RegisteredManager {

    private final Map<String, List<CommandWrapper<?>>> commands = new ConcurrentHashMap<>();
    private final CommandWrapperManager<?> wrapperManager;

    @Inject
    protected CommandManager(CommandWrapperManager<?> wrapperManager) {
        this.wrapperManager = wrapperManager;
    }

    public CommandWrapper<?> register(String declaringClass, ScriptObjectMirror script, String command) {
        return this.register(declaringClass, script, new String[]{command}).get(0);
    }

    public List<CommandWrapper<?>> register(String declaringClass, ScriptObjectMirror script, String[] cmds) {
        List<CommandWrapper<?>> newlyRegistedWrappers = new ArrayList<>();
        for(String command : cmds) {
            command = command.toLowerCase();

            List<CommandWrapper<?>> commands = this.commands.get(declaringClass);
            if(commands == null) {
                commands = new ArrayList<>();
                this.commands.put(declaringClass, commands);
            }

            CommandWrapper<?> wrapper = this.wrapperManager.createCommandWrapper(declaringClass, command, script);
            commands.add(wrapper);
            this.removeCommand(wrapper);
            this.registerCommand(wrapper);
            newlyRegistedWrappers.add(wrapper);
        }
        return newlyRegistedWrappers;
    }

    public void unregister(String declaringClass) {
        List<CommandWrapper<?>> commands = this.commands.get(declaringClass);
        if(commands != null) {
            for(CommandWrapper<?> wrapper : commands) {
                this.removeCommand(wrapper);
            }
        }
        this.commands.keySet().remove(declaringClass);
    }

    protected Map<String, List<CommandWrapper<?>>> getCommands() {
        return this.commands;
    }

    protected abstract boolean registerCommand(CommandWrapper<?> wrapper);

    protected abstract boolean removeCommand(CommandWrapper<?> wrapper);

}