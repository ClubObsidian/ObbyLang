package com.clubobsidian.obbylang.manager.velocity;

import com.clubobsidian.obbylang.manager.message.MessageManager;
import com.clubobsidian.obbylang.util.MessageUtil;
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