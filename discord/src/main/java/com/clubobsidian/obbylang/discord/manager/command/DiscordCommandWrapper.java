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

package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class DiscordCommandWrapper extends CommandWrapper<DiscordCommand, Object> {


    public DiscordCommandWrapper(Object owner, Object command, ScriptObjectMirror base) {
        super(owner, command, base);
    }

    @Override
    public DiscordCommand getCommand() {
        return new DiscordCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }

    @Override
    protected String getCommandName(Object command) {
        if(command != null) {
            if(command instanceof String) {
                return (String) command;
            } else if(command instanceof CommandData) {
                CommandData data = (CommandData) command;
                return data.getName();
            }
        }
        return null;
    }
}