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

package com.clubobsidian.obbylang.manager.message;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.util.ChatColor;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public abstract class MessageManager<T> {

    private static MessageManager<?> instance;

    private final MiniMessage miniMessage;
    private final GsonComponentSerializer serializer;

    @Inject
    protected MessageManager() {
        this.miniMessage = MiniMessage
                .builder()
                .markdown()
                .build();
        this.serializer = GsonComponentSerializer.builder().build();
    }

    public abstract void msg(T sender, String msg);

    public abstract void message(T sender, String msg);

    public abstract void messageJson(T sender, String json);

    public abstract void broadcast(String str);

    public abstract void broadcastJson(String json);

    public String parseMiniMessage(String message) {
        Component component = this.miniMessage.deserialize(message);
        return this.serializer.serialize(component);
    }

    public void miniMsg(T sender, String msg) {
        this.miniMessage(sender, msg);
    }

    public void miniMessage(T sender, String msg) {
        String json = this.parseMiniMessage(msg);
        this.messageJson(sender, json);
    }

    public void miniBroadcast(String msg) {
        String json = this.parseMiniMessage(msg);
        this.broadcastJson(json);
    }

    public String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}