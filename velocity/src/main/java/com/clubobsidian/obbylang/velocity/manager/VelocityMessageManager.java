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

package com.clubobsidian.obbylang.velocity.manager;

import com.clubobsidian.obbylang.manager.message.MessageManager;
import com.clubobsidian.obbylang.velocity.util.MessageUtil;
import com.velocitypowered.api.command.CommandSource;

public class VelocityMessageManager extends MessageManager<CommandSource> {

    @Override
    public void broadcast(String message) {
        MessageUtil.broadcast(this.color(message));
    }

    @Override
    public void broadcastJson(String json) {
        MessageUtil.broadcastJson(json);
    }

    @Override
    public void message(CommandSource sender, String message) {
        MessageUtil.sendMessage(sender, this.color(message));
    }

    @Override
    public void messageJson(CommandSource sender, String json) {
        MessageUtil.sendJsonMessage(sender, json);
    }

    @Override
    public void msg(CommandSource sender, String message) {
        MessageUtil.sendMessage(sender, this.color(message));
    }
}