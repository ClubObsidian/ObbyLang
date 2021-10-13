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

package com.clubobsidian.obbylang.velocity.util;

import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final GsonComponentSerializer JSON_SERIALIZER = GsonComponentSerializer.gson();

    public static void sendMessage(final CommandSource source, final String message) {
        Component serialized = SERIALIZER.deserialize(message);
        source.sendMessage(serialized);
    }

    public static void sendJsonMessage(final CommandSource source, final String json) {
        Component serialized = JSON_SERIALIZER.deserialize(json);
        source.sendMessage(serialized);
    }

    public static void broadcast(final String message) {
        Component serialized = SERIALIZER.deserialize(message);
        VelocityObbyLangPlugin
                .get().getServer()
                .sendMessage(serialized);
    }

    public static void broadcastJson(final String json) {
        Component serialized = JSON_SERIALIZER.deserialize(json);
        VelocityObbyLangPlugin
                .get().getServer()
                .sendMessage(serialized);
    }

    private MessageUtil() {
    }
}