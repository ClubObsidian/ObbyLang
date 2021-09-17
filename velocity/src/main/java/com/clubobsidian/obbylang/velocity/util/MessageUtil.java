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