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