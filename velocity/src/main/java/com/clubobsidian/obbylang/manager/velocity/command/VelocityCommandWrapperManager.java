package com.clubobsidian.obbylang.manager.velocity.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.velocitypowered.api.command.Command;
import org.graalvm.polyglot.Value;

public class VelocityCommandWrapperManager extends CommandWrapperManager<Command> {

    @Override
    public CommandWrapper<Command> createCommandWrapper(String declaringClass, String command, Value script) {
        return new VelocityCommandWrapper(script, command, script);
    }
}