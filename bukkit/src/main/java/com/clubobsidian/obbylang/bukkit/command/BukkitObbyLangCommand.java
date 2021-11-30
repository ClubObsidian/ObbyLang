/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.bukkit.command;

import com.clubobsidian.obbylang.bukkit.pipe.SenderPipe;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BukkitObbyLangCommand implements CommandExecutor {

    private final ScriptManager scriptManager;

    @Inject
    private BukkitObbyLangCommand(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("obbylang.use")) {
            if(args.length == 2) {
                Pipe pipe = new SenderPipe(sender);
                if(args[0].equalsIgnoreCase("load")) {
                    boolean loaded = this.scriptManager.loadScript(args[1], pipe);
                    if(loaded) {
                        sender.sendMessage("Script has been loaded");
                    } else {
                        sender.sendMessage("Script could not be loaded");
                    }
                } else if(args[0].equalsIgnoreCase("unload")) {
                    boolean unloaded = this.scriptManager.unloadScript(args[1], pipe);
                    if(unloaded) {
                        sender.sendMessage("Script has been unloaded");
                    } else {
                        sender.sendMessage("Script could not be unloaded");
                    }
                } else if(args[0].equalsIgnoreCase("reload")) {
                    boolean reload = this.scriptManager.reloadScript(args[1], pipe);
                    if(reload) {
                        sender.sendMessage("Script has been reloaded");
                    } else {
                        sender.sendMessage("Script could not be reloaded");
                        sender.sendMessage("Attemping to load the script");
                        boolean load = this.scriptManager.loadScript(args[1], pipe);
                        if(load) {
                            sender.sendMessage("Script has been loaded");
                        } else {
                            sender.sendMessage("Script could not be loaded");
                        }
                    }
                } else if(args[0].equalsIgnoreCase("enable")) {
                    boolean enable = this.scriptManager.enableScript(args[1], pipe);
                    if(enable) {
                        sender.sendMessage("Script has been enabled");
                    } else {
                        sender.sendMessage("Script can not be enabled");
                    }
                } else if(args[0].equalsIgnoreCase("disable")) {
                    boolean disable = this.scriptManager.disableScript(args[1], pipe);
                    if(disable) {
                        sender.sendMessage("Script has been disabled");
                    } else {
                        sender.sendMessage("Script can not be disabled");
                    }
                } else {
                    this.sendCommandList(label, sender);
                }
                return true;
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(this.scriptManager.getScriptListString());
                    return true;
                }
            }

            this.sendCommandList(label, sender);
            return true;

        }
        return false;
    }

    private void sendCommandList(String label, CommandSender sender) {
        sender.sendMessage("/" + label + " load <script>");
        sender.sendMessage("/" + label + " unload <script>");
        sender.sendMessage("/" + label + " reload <script>");
        sender.sendMessage("/" + label + " enable <script>");
        sender.sendMessage("/" + label + " disable <script>");
        sender.sendMessage("/" + label + " list");
    }
}