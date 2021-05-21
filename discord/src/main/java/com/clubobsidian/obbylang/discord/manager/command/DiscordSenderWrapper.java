package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import net.dv8tion.jda.api.entities.User;

public class DiscordSenderWrapper extends SenderWrapper<User> {

    /*
     * Just a stub, this isn't used
     */

    public DiscordSenderWrapper(User sender) {
        super(sender);
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
        return null;
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
        return false;
    }

    @Override
    public void sendMessage(String message) {
        return;
    }

    @Override
    public void sendMessage(String[] messages) {
        return;
    }
}