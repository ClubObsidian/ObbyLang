package com.clubobsidian.obbylang.command;

import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.pipe.bungeecord.SenderPipe;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class ObbyLangCommand extends Command {

    public ObbyLangCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender.hasPermission("obbylang.use")) {
            if(args.length == 2) {
                Pipe pipe = new SenderPipe(sender);
                if(args[0].equalsIgnoreCase("load")) {
                    boolean loaded = ScriptManager.get().loadScript(args[1], pipe);
                    if(loaded) {
                        sender.sendMessage("Script has been loaded");
                    } else {
                        sender.sendMessage("Script could not be loaded");
                    }
                } else if(args[0].equalsIgnoreCase("unload")) {
                    boolean unloaded = ScriptManager.get().unloadScript(args[1], pipe);
                    if(unloaded) {
                        sender.sendMessage("Script has been unloaded");
                    } else {
                        sender.sendMessage("Script could not be unloaded");
                    }
                } else if(args[0].equalsIgnoreCase("reload")) {
                    boolean reload = ScriptManager.get().reloadScript(args[1], pipe);
                    if(reload) {
                        sender.sendMessage("Script has been reloaded");
                    } else {
                        sender.sendMessage("Script could not be reloaded");
                        sender.sendMessage("Attemping to load the script");
                        boolean load = ScriptManager.get().loadScript(args[1], pipe);
                        if(load) {
                            sender.sendMessage("Script has been loaded");
                        } else {
                            sender.sendMessage("Script could not be loaded");
                        }
                    }
                } else if(args[0].equalsIgnoreCase("enable")) {
                    boolean enable = ScriptManager.get().enableScript(args[1], pipe);
                    if(enable) {
                        sender.sendMessage("Script has been enabled");
                    } else {
                        sender.sendMessage("Script can not be enabled");
                    }
                } else if(args[0].equalsIgnoreCase("disable")) {
                    boolean disable = ScriptManager.get().disableScript(args[1], pipe);
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
                    sender.sendMessage(ScriptManager.get().getScriptListString());
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