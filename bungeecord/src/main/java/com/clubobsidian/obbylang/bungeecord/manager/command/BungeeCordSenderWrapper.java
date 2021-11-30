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

package com.clubobsidian.obbylang.bungeecord.manager.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.clubobsidian.obbylang.util.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;

public class BungeeCordSenderWrapper extends SenderWrapper<CommandSender> implements CommandSender {

    public BungeeCordSenderWrapper(CommandSender sender) {
        super(sender);
    }

    @Override
    public String getName() {
        return this.getOriginalSender().getName();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendMessage(String message) {
        this.getOriginalSender().sendMessage(ChatColor.translateAlternateColorCodes(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        for(String msg : messages) {
            this.sendMessage(msg);
        }
    }

    @Override
    public void sendMessages(String... messages) {
        for(String msg : messages) {
            this.sendMessage(msg);
        }
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        this.getOriginalSender().sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent message) {
        this.getOriginalSender().sendMessage(message);
    }

    @Override
    public Collection<String> getGroups() {
        return this.getOriginalSender().getGroups();
    }

    @Override
    public void addGroups(String... groups) {
        this.getOriginalSender().addGroups(groups);
    }

    @Override
    public void removeGroups(String... groups) {
        this.getOriginalSender().removeGroups(groups);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.getOriginalSender().hasPermission(permission);
    }

    @Override
    public void setPermission(String permission, boolean value) {
        this.getOriginalSender().setPermission(permission, value);
    }

    @Override
    public Collection<String> getPermissions() {
        return this.getOriginalSender().getPermissions();
    }

    @Override
    public Object asCommandBlock() {
        return null;
    }

    @Override
    public Object asConsole() {
        return null;
    }

    @Override
    public Object asPlayer() {
        return this.getOriginalSender();
    }

    @Override
    public boolean isCommandBlock() {
        return false;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return this.getOriginalSender() instanceof ProxiedPlayer;
    }


}