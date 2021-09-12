package com.clubobsidian.obbylang.bukkit.pipe;

import com.clubobsidian.obbylang.pipe.Pipe;
import org.bukkit.command.CommandSender;

public class SenderPipe implements Pipe {

    private final CommandSender sender;

    public SenderPipe(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void out(String msg) {
        this.sender.sendMessage(msg);
    }
}