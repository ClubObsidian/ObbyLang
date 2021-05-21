package com.clubobsidian.obbylang.command;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.pipe.velocity.SourcePipe;
import com.clubobsidian.obbylang.util.MessageUtil;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;


public class ObbyLangCommand implements Command {

    @Override
    public void execute(CommandSource source, String @NonNull [] args) {
        if(source.hasPermission("obbylang.use")) {
            if(args.length == 2) {
                Pipe pipe = new SourcePipe(source);
                if(args[0].equalsIgnoreCase("load")) {
                    boolean loaded = ScriptManager.get().loadScript(args[1], pipe);
                    if(loaded) {
                        MessageUtil.sendMessage(source, "Script has been loaded");
                    } else {
                        MessageUtil.sendMessage(source, "Script could not be loaded");
                    }
                } else if(args[0].equalsIgnoreCase("unload")) {
                    boolean unloaded = ScriptManager.get().unloadScript(args[1], pipe);
                    if(unloaded) {
                        MessageUtil.sendMessage(source, "Script has been unloaded");
                    } else {
                        MessageUtil.sendMessage(source, "Script could not be unloaded");
                    }
                } else if(args[0].equalsIgnoreCase("reload")) {
                    boolean reload = ScriptManager.get().reloadScript(args[1], pipe);
                    if(reload) {
                        MessageUtil.sendMessage(source, "Script has been reloaded");
                    } else {
                        MessageUtil.sendMessage(source, "Script could not be reloaded");
                        MessageUtil.sendMessage(source, "Attemping to load the script");
                        boolean load = ScriptManager.get().loadScript(args[1], pipe);
                        if(load) {
                            MessageUtil.sendMessage(source, "Script has been loaded");
                        } else {
                            MessageUtil.sendMessage(source, "Script could not be loaded");
                        }
                    }
                } else if(args[0].equalsIgnoreCase("enable")) {
                    boolean enable = ScriptManager.get().enableScript(args[1], pipe);
                    if(enable) {
                        MessageUtil.sendMessage(source, "Script has been enabled");
                    } else {
                        MessageUtil.sendMessage(source, "Script can not be enabled");
                    }
                } else if(args[0].equalsIgnoreCase("disable")) {
                    boolean disable = ScriptManager.get().disableScript(args[1], pipe);
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
                    MessageUtil.sendMessage(source, ScriptManager.get().getScriptListString());
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