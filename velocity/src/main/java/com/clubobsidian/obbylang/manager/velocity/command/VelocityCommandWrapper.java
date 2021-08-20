package com.clubobsidian.obbylang.manager.velocity.command;

import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.velocitypowered.api.command.Command;
import org.graalvm.polyglot.Value;

public class VelocityCommandWrapper extends CommandWrapper<Command> {

    public VelocityCommandWrapper(Object owner, String commandName, Value base) {
        super(owner, commandName, base);
    }

    @Override
    public Command getCommand() {
        return new VelocityCommand(this.getOwner(), this.getCommandName(), this.getBase());
    }
}