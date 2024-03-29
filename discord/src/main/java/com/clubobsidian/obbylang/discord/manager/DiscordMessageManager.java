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

package com.clubobsidian.obbylang.discord.manager;

import com.clubobsidian.obbylang.manager.message.MessageManager;
import net.dv8tion.jda.api.entities.MessageChannel;

public class DiscordMessageManager extends MessageManager<MessageChannel> {

    @Override
    public void broadcast(String message) {
        throw new UnsupportedOperationException("Broadcast cannot be used on ObbyLang for Discord.");
    }

    @Override
    public void broadcastJson(String json) {
        throw new UnsupportedOperationException("Broadcast cannot be used on ObbyLang for Discord.");
    }

    @Override
    public void message(MessageChannel channel, String message) {
        channel.sendMessage(message);
    }

    @Override
    public void messageJson(MessageChannel sender, String json) {
        throw new UnsupportedOperationException("Json messaging is not implemented in ObbyLang for Discord.");
    }

    @Override
    public void msg(MessageChannel channel, String message) {
        this.message(channel, message);
    }
}