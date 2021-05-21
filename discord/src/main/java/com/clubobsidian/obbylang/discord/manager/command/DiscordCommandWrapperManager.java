package com.clubobsidian.obbylang.discord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class DiscordCommandWrapperManager extends CommandWrapperManager<DiscordCommand> {

    @Override
    public CommandWrapper<DiscordCommand> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script) {
        return new DiscordCommandWrapper(script, command, script);
    }
}