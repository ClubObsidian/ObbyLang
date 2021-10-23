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

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.google.inject.Inject;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DiscordCommandManager extends CommandManager {

    private Map<String, DiscordCommand> registeredCommands = new ConcurrentHashMap<>();

    @Inject
    protected DiscordCommandManager(CommandWrapperManager<?, ?> wrapperManager) {
        super(wrapperManager);
    }

    @Override
    protected boolean registerCommand(CommandWrapper<?, ?> wrapper) {
        if(wrapper.getCommand() instanceof DiscordCommand) {
            String commandName = wrapper.getCommandName();
            DiscordCommand cmd = (DiscordCommand) wrapper.getCommand();
            this.removeCommand(wrapper);
            this.registeredCommands.put(commandName, cmd);
            return true;
        }

        return false;
    }

    @Override
    protected boolean removeCommand(CommandWrapper<?, ?> wrapper) {
        if(wrapper.getCommand() instanceof DiscordCommand) {
            String commandName = wrapper.getCommandName();
            super.unregister(wrapper.getCommandName());
            return this.registeredCommands.remove(commandName) != null;
        }

        return false;
    }

    public Map<String, DiscordCommand> getRegisteredCommands() {
        return this.registeredCommands;
    }

    @SubscribeEvent
    public void onEvent(MessageReceivedEvent event) {

        String message = event.getMessage().getContentRaw();
        String label = message;
        String[] args = new String[0];
        if(message.contains(" ")) {
            String[] split = message.split(" ");
            label = split[0];

            if(split.length > 1) {
                args = Arrays.copyOfRange(split, 1, split.length);
            }
        }

        DiscordCommand command = this.registeredCommands.get(label);
        if(command != null) {
            User user = event.getAuthor();
            MessageChannel channel = event.getChannel();
            command.onCommand(user, channel, args);
        }
    }
}