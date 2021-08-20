package com.clubobsidian.obbylang.manager.bukkit.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import org.bukkit.command.Command;
import org.graalvm.polyglot.Value;

public class BukkitCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, Value script) {
        return new BukkitCommandWrapper(script, command, script);
    }
}