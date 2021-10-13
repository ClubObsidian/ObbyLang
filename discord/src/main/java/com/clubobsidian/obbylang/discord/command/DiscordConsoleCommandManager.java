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

package com.clubobsidian.obbylang.discord.command;

import com.clubobsidian.obbylang.discord.pipe.LoggerPipe;
import com.clubobsidian.obbylang.discord.plugin.DiscordObbyLangPlugin;
import com.clubobsidian.obbylang.pipe.Pipe;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DiscordConsoleCommandManager {

    private Map<String, Command> commands;
    private Pipe consolePipe;

    public DiscordConsoleCommandManager() {
        this.commands = new ConcurrentHashMap<>();
        this.consolePipe = new LoggerPipe();
    }

    public boolean registerCommand(Command command) {
        return this.registerCommand(command, false);
    }

    public boolean registerCommand(Command command, boolean force) {
        if(force) {
            this.commands.put(command.getName(), command);
            return true;
        }

        if(this.commandExists(command.getName())) {
            return false;
        }

        this.commands.put(command.getName(), command);
        return true;
    }

    public boolean commandExists(String command) {
        return this.commands.keySet().contains(command);
    }

    public boolean dispatchCommand(String command) {
        Logger logger = DiscordObbyLangPlugin.get().getLogger();
        logger.info(this.commands.toString());
        if(command.length() <= 0) {
            logger.info("Invalid command length for command, length is: " + command.length());
            return false;
        }

        String[] args = command.split(" ");
        if(args.length > 0) {
            String name = args[0];
            if(this.commandExists(name)) {
                if(args.length > 1) {
                    args = Arrays.copyOfRange(args, 1, args.length);
                } else {
                    args = new String[0];
                }

                Command cmd = this.commands.get(name);
                cmd.onCommand(this.consolePipe, args);
                return true;
            } else {
                logger.info("Unknown command " + name + " please try again");
            }
        }
        return false;
    }
}