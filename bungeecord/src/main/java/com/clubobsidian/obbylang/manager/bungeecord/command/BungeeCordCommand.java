package com.clubobsidian.obbylang.manager.bungeecord.command;

import com.clubobsidian.obbylang.manager.command.SenderWrapper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

public class BungeeCordCommand extends Command {

    private final Object owner;
    private final String command;
    private final Value base;

    public BungeeCordCommand(Object owner, String command, Value base) {
        super(command);
        this.owner = owner;
        this.command = command;
        this.base = base;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SenderWrapper<?> wrapper = new BungeeCordSenderWrapper(sender);
        Map<String, Object> properties = new HashMap<>();
        properties.put("args", args);
        properties.put("sender", wrapper);
        this.base.executeVoid(wrapper, this, this.command, args);
    }
}