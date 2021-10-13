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

package com.clubobsidian.obbylang.velocity.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;
import com.velocitypowered.api.command.Command;

import java.lang.reflect.Field;
import java.util.Map;

public class VelocityCommandManager extends CommandManager {

    private Map<String, Command> cm = null;

    @SuppressWarnings("unchecked")
    private final Map<String, Command> getCommandMap() {
        if(this.cm == null) {
            try {
                com.velocitypowered.api.command.CommandManager pluginManager = VelocityObbyLangPlugin
                        .get()
                        .getServer()
                        .getCommandManager();
                Field f = pluginManager.getClass().getDeclaredField("commands");
                f.setAccessible(true);
                this.cm = (Map<String, Command>) f.get(pluginManager);
            } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return this.cm;
    }

    @Override
    protected boolean registerCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            Command cmd = (Command) wrapper.getCommand();
            this.removeCommand(wrapper);
            this.getCommandMap().put(wrapper.getCommandName(), cmd);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removeCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            String commandName = wrapper.getCommandName();
            this.getCommandMap().remove(commandName);
        }
        return false;
    }
}