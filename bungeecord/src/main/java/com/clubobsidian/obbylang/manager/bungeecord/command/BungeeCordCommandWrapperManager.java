package com.clubobsidian.obbylang.manager.bungeecord.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCordCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script) {
        return new BungeeCordCommandWrapper(script, command, script);
    }
}