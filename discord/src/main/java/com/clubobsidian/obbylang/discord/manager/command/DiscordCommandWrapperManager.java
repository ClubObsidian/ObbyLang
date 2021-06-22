package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import org.graalvm.polyglot.Value;

public class DiscordCommandWrapperManager extends CommandWrapperManager<DiscordCommand> {

    @Override
    public CommandWrapper<DiscordCommand> createCommandWrapper(String declaringClass, String command, Value script) {
        return new DiscordCommandWrapper(script, command, script);
    }
}