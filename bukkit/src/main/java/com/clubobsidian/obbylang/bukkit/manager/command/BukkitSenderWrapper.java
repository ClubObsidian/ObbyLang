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

package com.clubobsidian.obbylang.bukkit.manager.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.clubobsidian.obbylang.util.ChatColor;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class BukkitSenderWrapper extends SenderWrapper<CommandSender> implements CommandSender {

    public BukkitSenderWrapper(CommandSender sender) {
        super(sender);
    }

    @Override
    public Object asCommandBlock() {
        if(this.isCommandBlock()) {
            return this.getOriginalSender();
        }
        return null;
    }

    @Override
    public Object asConsole() {
        if(this.isConsole()) {
            return this.getOriginalSender();
        }
        return null;
    }

    @Override
    public Object asPlayer() {
        if(this.isPlayer()) {
            return this.getOriginalSender();
        }
        return null;
    }

    @Override
    public boolean isCommandBlock() {
        return this.getOriginalSender() instanceof CommandBlock;
    }

    @Override
    public boolean isConsole() {
        return this.getOriginalSender() instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer() {
        return this.getOriginalSender() instanceof Player;
    }

    @Override
    public void sendMessage(String message) {
        this.getOriginalSender().sendMessage(ChatColor.translateAlternateColorCodes(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        for(String message : messages) {
            this.sendMessage(message);
        }
    }
}