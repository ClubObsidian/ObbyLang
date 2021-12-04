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

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;

import javax.inject.Inject;

public class VelocityCommandManager extends CommandManager {

    private final com.velocitypowered.api.command.CommandManager cmdManager;

    @Inject
    protected VelocityCommandManager(CommandWrapperManager<?> wrapperManager) {
        super(wrapperManager);
        this.cmdManager = VelocityObbyLangPlugin
                .get()
                .getServer()
                .getCommandManager();
    }

    @Override
    protected boolean registerCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            Command cmd = (Command) wrapper.getCommand();
            String commandName = wrapper.getCommandName();
            this.cmdManager.unregister(commandName);
            CommandMeta meta = this.cmdManager.metaBuilder(commandName).build();
            this.cmdManager.register(meta, cmd);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removeCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            String commandName = wrapper.getCommandName();
            this.cmdManager.unregister(commandName);
            return true;
        }
        return false;
    }
}