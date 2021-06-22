package com.clubobsidian.obbylang.manager.bungeecord.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import net.md_5.bungee.api.plugin.Command;
import org.graalvm.polyglot.Value;

public class BungeeCordCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, Value script) {
        return new BungeeCordCommandWrapper(script, command, script);
    }
}