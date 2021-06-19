package com.clubobsidian.obbylang.manager.bukkit.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.command.Command;

public class BukkitCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script) {
        return new BukkitCommandWrapper(script, command, script);
    }
}