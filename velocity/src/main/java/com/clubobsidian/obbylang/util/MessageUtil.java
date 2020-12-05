package com.clubobsidian.obbylang.util;

import com.clubobsidian.obbylang.plugin.VelocityObbyLangPlugin;
import com.velocitypowered.api.command.CommandSource;

import net.kyori.text.Component;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {

	private static LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer
			.legacy();
	
	private MessageUtil() {}
	
	public static void sendMessage(final CommandSource source, final String message) {
		Component serialized = SERIALIZER.deserialize(message);
		source.sendMessage(serialized);
	}

	public static void broadcast(final String message) {
		Component serialized = SERIALIZER.deserialize(message);
		VelocityObbyLangPlugin
		.get().getServer()
		.broadcast(serialized);
	}
}