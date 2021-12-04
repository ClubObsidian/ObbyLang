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

package com.clubobsidian.obbylang.velocity.command;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.velocity.pipe.SourcePipe;
import com.clubobsidian.obbylang.velocity.util.MessageUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import javax.inject.Inject;


public class VelocityObbyLangCommand implements SimpleCommand {

    private final ScriptManager scriptManager;

    @Inject
    private VelocityObbyLangCommand(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if(source.hasPermission("obbylang.use")) {
            String[] args = invocation.arguments();
            if(args.length == 2) {
                Pipe pipe = new SourcePipe(source);
                if(args[0].equalsIgnoreCase("load")) {
                    boolean loaded = this.scriptManager.loadScript(args[1], pipe);
                    if(loaded) {
                        MessageUtil.sendMessage(source, "Script has been loaded");
                    } else {
                        MessageUtil.sendMessage(source, "Script could not be loaded");
                    }
                } else if(args[0].equalsIgnoreCase("unload")) {
                    boolean unloaded = this.scriptManager.unloadScript(args[1], pipe);
                    if(unloaded) {
                        MessageUtil.sendMessage(source, "Script has been unloaded");
                    } else {
                        MessageUtil.sendMessage(source, "Script could not be unloaded");
                    }
                } else if(args[0].equalsIgnoreCase("reload")) {
                    boolean reload = this.scriptManager.reloadScript(args[1], pipe);
                    if(reload) {
                        MessageUtil.sendMessage(source, "Script has been reloaded");
                    } else {
                        MessageUtil.sendMessage(source, "Script could not be reloaded");
                        MessageUtil.sendMessage(source, "Attemping to load the script");
                        boolean load = this.scriptManager.loadScript(args[1], pipe);
                        if(load) {
                            MessageUtil.sendMessage(source, "Script has been loaded");
                        } else {
                            MessageUtil.sendMessage(source, "Script could not be loaded");
                        }
                    }
                } else if(args[0].equalsIgnoreCase("enable")) {
                    boolean enable = this.scriptManager.enableScript(args[1], pipe);
                    if(enable) {
                        MessageUtil.sendMessage(source, "Script has been enabled");
                    } else {
                        MessageUtil.sendMessage(source, "Script can not be enabled");
                    }
                } else if(args[0].equalsIgnoreCase("disable")) {
                    boolean disable = this.scriptManager.disableScript(args[1], pipe);
                    if(disable) {
                        MessageUtil.sendMessage(source, "Script has been disabled");
                    } else {
                        MessageUtil.sendMessage(source, "Script can not be disabled");
                    }
                } else {
                    this.sendCommandList(source);
                }
                return;
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("list")) {
                    MessageUtil.sendMessage(source, this.scriptManager.getScriptListString());
                    return;
                }
            }

            this.sendCommandList(source);
            return;

        }
        return;
    }

    private void sendCommandList(CommandSource source) {
        MessageUtil.sendMessage(source, "gol load <script>");
        MessageUtil.sendMessage(source, "gol unload <script>");
        MessageUtil.sendMessage(source, "gol reload <script>");
        MessageUtil.sendMessage(source, "gol enable <script>");
        MessageUtil.sendMessage(source, "gol disable <script>");
        MessageUtil.sendMessage(source, "gol list");
    }
}