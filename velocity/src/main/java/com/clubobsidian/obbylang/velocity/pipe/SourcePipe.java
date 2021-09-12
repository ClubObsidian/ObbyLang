package com.clubobsidian.obbylang.velocity.pipe;

import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.velocity.util.MessageUtil;
import com.velocitypowered.api.command.CommandSource;

public class SourcePipe implements Pipe {

    private final CommandSource sender;

    public SourcePipe(CommandSource sender) {
        this.sender = sender;
    }

    @Override
    public void out(String message) {
        MessageUtil.sendMessage(this.sender, message);
    }
}