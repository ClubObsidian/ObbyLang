/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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