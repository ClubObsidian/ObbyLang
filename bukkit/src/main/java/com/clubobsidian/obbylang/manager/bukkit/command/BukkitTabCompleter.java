package com.clubobsidian.obbylang.manager.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.graalvm.polyglot.Value;

import java.util.List;

public class BukkitTabCompleter implements TabCompleter {

    private final Object owner;
    private final Value script;

    public BukkitTabCompleter(Object owner, Value base) {
        this.owner = owner;
        this.script = base;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        BukkitSenderWrapper senderWrapper = new BukkitSenderWrapper(sender);
        Object call = this.script.execute(senderWrapper, command, alias, args);
        if(call == null || !(call instanceof List)) {
            return null;
        }
        return (List<String>) call;
    }
}