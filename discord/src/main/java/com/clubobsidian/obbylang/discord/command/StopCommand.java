package com.clubobsidian.obbylang.discord.command;

import com.clubobsidian.obbylang.discord.Bootstrap;
import com.clubobsidian.obbylang.pipe.Pipe;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop");
    }

    @Override
    public boolean onCommand(Pipe pipe, String[] args) {
        pipe.out("Shutting down the server...");
        Bootstrap.getPlugin().onDisable();
        System.exit(0);
        return true;
    }

}
