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

package com.clubobsidian.obbylang.bungeecord.command;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.bungeecord.pipe.SenderPipe;
import com.google.inject.Inject;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCordObbyLangCommand extends Command {

    private final ScriptManager scriptManager;
    @Inject
    private BungeeCordObbyLangCommand(ScriptManager scriptManager) {
        super("gobbylang", null, "gol");
        this.scriptManager = scriptManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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
                    this.sendCommandList(sender);
                }
                return;
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(this.scriptManager.getScriptListString());
                    return;
                }
            }

            this.sendCommandList(sender);
            return;

        }
        return;
    }

    private void sendCommandList(CommandSender sender) {
        sender.sendMessage("gol load <script>");
        sender.sendMessage("gol unload <script>");
        sender.sendMessage("gol reload <script>");
        sender.sendMessage("gol enable <script>");
        sender.sendMessage("gol disable <script>");
        sender.sendMessage("gol list");
    }
}