package com.clubobsidian.obbylang.velocity.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.velocitypowered.api.command.Command;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class VelocityCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script) {
        return new VelocityCommandWrapper(script, command, script);
    }
}