package com.clubobsidian.obbylang.discord.manager.command;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordCommand {

    private Object owner;
    private String command;
    private ScriptObjectMirror base;

    public DiscordCommand(Object owner, String command, ScriptObjectMirror base) {
        this.owner = owner;
        this.command = command;
        this.base = base;
    }

    public boolean onCommand(User user, MessageChannel channel, String[] args) {
        boolean isPrivate = channel instanceof PrivateChannel;
        Object ret = this.base.call(this.owner, user, channel, isPrivate, this.command, args);
        if(ret != null && ret instanceof Boolean) {
            return (boolean) ret;
        }

        return false;
    }
}