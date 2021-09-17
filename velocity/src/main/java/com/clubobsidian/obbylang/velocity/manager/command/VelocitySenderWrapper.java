package com.clubobsidian.obbylang.velocity.manager.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.clubobsidian.obbylang.util.ChatColor;
import com.clubobsidian.obbylang.velocity.util.MessageUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class VelocitySenderWrapper extends SenderWrapper<CommandSource> implements CommandSource {

    private final static String NA = "N/A";

    public VelocitySenderWrapper(CommandSource sender) {
        super(sender);
    }

    public String getName() {
        if(this.getOriginalSender() instanceof Player) {
            Player player = (Player) this.getOriginalSender();
            return player.getUsername();
        }
        return NA;
    }

    @Override
    public void sendMessage(String message) {
        MessageUtil.sendMessage(this.getOriginalSender(), ChatColor.translateAlternateColorCodes(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        for(String msg : messages) {
            this.sendMessage(msg);
        }
    }

    public void sendMessages(String... messages) {
        for(String msg : messages) {
            this.sendMessage(msg);
        }
    }

    @Override
    public void sendMessage(Component message) {
        this.getOriginalSender().sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.getOriginalSender().hasPermission(permission);
    }

    @Override
    public Object asCommandBlock() {
        return null;
    }

    @Override
    public Object asConsole() {
        return null;
    }

    @Override
    public Object asPlayer() {
        return this.getOriginalSender();
    }

    @Override
    public boolean isCommandBlock() {
        return false;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return this.getOriginalSender() instanceof Player;
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        return this.getOriginalSender().getPermissionValue(permission);
    }
}