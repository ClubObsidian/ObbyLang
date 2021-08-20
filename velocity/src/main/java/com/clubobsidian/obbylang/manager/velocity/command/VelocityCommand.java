package com.clubobsidian.obbylang.manager.velocity.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

public class VelocityCommand implements Command {

    private final Object owner;
    private final String command;
    private final Value base;

    public VelocityCommand(Object owner, String command, Value base) {
        this.owner = owner;
        this.command = command;
        this.base = base;
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        SenderWrapper<?> wrapper = new VelocitySenderWrapper(sender);
        Map<String, Object> properties = new HashMap<>();
        properties.put("args", args);
        properties.put("sender", wrapper);

        this.base.executeVoid(wrapper, this, this.command, args);
    }
}