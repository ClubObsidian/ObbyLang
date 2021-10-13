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

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.clubobsidian.obbylang.util.ChatColor;
import com.clubobsidian.obbylang.velocity.util.MessageUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class VelocitySenderWrapper extends SenderWrapper<CommandSource> implements CommandSource {

    private final static String NA = "N/A";

    public VelocitySenderWrapper(CommandSource sender) {
        super(sender);
    }

    public String getName() {
        if(this.getOriginalSender() instanceof Player) {
            Player player = (Player) this.getOriginalSender();
            return player.getUsername();
        }
        return NA;
    }

    @Override
    public void sendMessage(String message) {
        MessageUtil.sendMessage(this.getOriginalSender(), ChatColor.translateAlternateColorCodes(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        for(String msg : messages) {
            this.sendMessage(msg);
        }
    }

    public void sendMessages(String... messages) {
        for(String msg : messages) {
            this.sendMessage(msg);
        }
    }

    @Override
    public void sendMessage(Component message) {
        this.getOriginalSender().sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.getOriginalSender().hasPermission(permission);
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
        return this.getOriginalSender() instanceof Player;
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        return this.getOriginalSender().getPermissionValue(permission);
    }
}