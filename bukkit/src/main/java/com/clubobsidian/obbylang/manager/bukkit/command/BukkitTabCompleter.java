package com.clubobsidian.obbylang.manager.bukkit.command;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class BukkitTabCompleter implements TabCompleter {

    private final Object owner;
    private final ScriptObjectMirror script;

    public BukkitTabCompleter(Object owner, ScriptObjectMirror base) {
        this.owner = owner;
        this.script = base;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        BukkitSenderWrapper senderWrapper = new BukkitSenderWrapper(sender);
        Object call = script.call(owner, senderWrapper, command, alias, args);
        if(call == null || !(call instanceof List)) {
            return null;
        }

        return (List<String>) call;
    }
}