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

package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.manager.message.MessageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitMessageManager extends MessageManager<CommandSender> {

    @Override
    public void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(this.color(message));
    }

    @Override
    public void broadcastJson(String json) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        Bukkit.getServer().spigot().broadcast(components);
    }

    @Override
    public void message(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }

    @Override
    public void messageJson(CommandSender sender, String json) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        if(sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(components);
        } else {
            String legacyText = TextComponent.toLegacyText(components);
            sender.sendMessage(legacyText);
        }
    }

    @Override
    public void msg(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }
}