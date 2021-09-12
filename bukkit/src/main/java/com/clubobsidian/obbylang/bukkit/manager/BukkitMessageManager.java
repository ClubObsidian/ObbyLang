package com.clubobsidian.obbylang.bukkit.manager;

import com.clubobsidian.obbylang.manager.message.MessageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitMessageManager extends MessageManager<CommandSender> {

    @Override
    public void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(this.color(message));
    }

    @Override
    public void broadcastJson(String json) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        Bukkit.getServer().spigot().broadcast(components);
    }

    @Override
    public void message(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }

    @Override
    public void messageJson(CommandSender sender, String json) {
        BaseComponent[] components = ComponentSerializer.parse(json);
        if(sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(components);
        } else {
            String legacyText = TextComponent.toLegacyText(components);
            sender.sendMessage(legacyText);
        }
    }

    @Override
    public void msg(CommandSender sender, String message) {
        sender.sendMessage(this.color(message));
    }
}