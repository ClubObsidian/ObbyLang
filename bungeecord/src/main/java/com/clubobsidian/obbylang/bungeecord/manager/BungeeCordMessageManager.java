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

package com.clubobsidian.obbylang.bungeecord.manager;

import com.clubobsidian.obbylang.manager.message.MessageManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class BungeeCordMessageManager extends MessageManager<CommandSender> {

    @Override
    public void broadcast(String message) {
        ProxyServer.getInstance().broadcast(this.color(message));
    }

    @Override
    public void broadcastJson(String json) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        ProxyServer.getInstance().broadcast(components);
    }

    @Override
    public void message(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }

    @Override
    public void messageJson(CommandSender sender, String json) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        sender.sendMessage(components);
    }

    @Override
    public void msg(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }
}