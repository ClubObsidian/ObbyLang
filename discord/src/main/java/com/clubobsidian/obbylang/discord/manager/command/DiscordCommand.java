package com.clubobsidian.obbylang.discord.manager.command;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import org.graalvm.polyglot.Value;

public class DiscordCommand {

    private final Object owner;
    private final String command;
    private final Value base;

    public DiscordCommand(Object owner, String command, Value base) {
        this.owner = owner;
        this.command = command;
        this.base = base;
    }

    public boolean onCommand(User user, MessageChannel channel, String[] args) {
        boolean isPrivate = channel instanceof PrivateChannel;
        Object ret = this.base.execute(user, channel, isPrivate, this.command, args);
        if(ret != null && ret instanceof Boolean) {
            return (boolean) ret;
        }

        return false;
    }
}