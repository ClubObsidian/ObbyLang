package com.clubobsidian.obbylang.bukkit.command;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class ObbyLangCommandTabCompleter implements TabCompleter {

    private final List<String> obbyLangArgs;

    public ObbyLangCommandTabCompleter() {
        this.obbyLangArgs = Arrays.asList(
                "load",
                "unload",
                "reload",
                "enable",
                "disable",
                "list"
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(sender.hasPermission("obbylang.use")) {
            if(args.length == 1) {
                return StringUtil.copyPartialMatches(args[args.length - 1], obbyLangArgs, Lists.newArrayList());
            } else if(args.length == 2 && !args[0].equalsIgnoreCase("list")) {
                if(obbyLangArgs.contains(args[0])) {
                    return StringUtil.copyPartialMatches(args[args.length - 1], ScriptManager.get().getScriptNames(), Lists.newArrayList());
                }
            }
        }
        return null;
    }
}