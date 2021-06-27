package com.clubobsidian.obbylang.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.pipe.Pipe;
import com.clubobsidian.obbylang.pipe.bukkit.SenderPipe;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ObbyLangCommand {

    @CommandMethod("obbylang|ol")
    @CommandPermission("obbylang.use")
    public void base(CommandSender sender) {
        this.sendCommandList(sender);
    }

    @CommandMethod("obbylang load <script>")
    public void load(CommandSender sender, @Argument(value = "script", suggestions = "script") String script) {
        Pipe pipe = new SenderPipe(sender);
        boolean loaded = ScriptManager.get().loadScript(script, pipe);
        if(loaded) {
            sender.sendMessage("Script has been loaded");
        } else {
            sender.sendMessage("Script could not be loaded");
        }
    }

    @CommandMethod("obbylang unload <script>")
    public void unload(CommandSender sender, @Argument(value = "script", suggestions = "script") String script) {
        Pipe pipe = new SenderPipe(sender);
        boolean unloaded = ScriptManager.get().unloadScript(script, pipe);
        if(unloaded) {
            sender.sendMessage("Script has been unloaded");
        } else {
            sender.sendMessage("Script could not be unloaded");
        }
    }

    @CommandMethod("obbylang reload <script>")
    public void reload(CommandSender sender, @Argument(value = "script", suggestions = "script") String script) {
        Pipe pipe = new SenderPipe(sender);
        boolean reload = ScriptManager.get().reloadScript(script, pipe);
        if(reload) {
            sender.sendMessage("Script has been reloaded");
        } else {
            sender.sendMessage("Script could not be reloaded");
            sender.sendMessage("Attemping to load the script");
            boolean load = ScriptManager.get().loadScript(script, pipe);
            if(load) {
                sender.sendMessage("Script has been loaded");
            } else {
                sender.sendMessage("Script could not be loaded");
            }
        }
    }

    @CommandMethod("obbylang enable <script>")
    public void enable(CommandSender sender, @Argument(value = "script", suggestions = "script") String script) {
        Pipe pipe = new SenderPipe(sender);
        boolean enabled = ScriptManager.get().enableScript(script, pipe);
        if(enabled) {
            sender.sendMessage("Script has been enabled");
        } else {
            sender.sendMessage("Script can not be enabled");
        }
    }

    @CommandMethod("obbylang disable <script>")
    public void disable(CommandSender sender, @Argument(value = "script", suggestions = "script") String script) {
        Pipe pipe = new SenderPipe(sender);
        boolean disabled = ScriptManager.get().disableScript(script, pipe);
        if(disabled) {
            sender.sendMessage("Script has been disabled");
        } else {
            sender.sendMessage("Script can not be disabled");
        }
    }

    @CommandMethod("obbylang list")
    public void list(CommandSender sender) {
        sender.sendMessage(ScriptManager.get().getScriptListString());
    }

    @Suggestions("script")
    public List<String> scriptSuggestions(CommandContext<CommandSender> sender, String input) {
        return ScriptManager.get().getScriptNames();
    }

    /*@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                    this.sendCommandList(label, sender);
                }
                return true;
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage(ScriptManager.get().getScriptListString());
                    return true;
                }
            }

            this.sendCommandList(label, sender);
            return true;

        }
        return false;
    }*/

    private void sendCommandList(CommandSender sender) {
        sender.sendMessage("/ol load <script>");
        sender.sendMessage("/ol unload <script>");
        sender.sendMessage("/ol reload <script>");
        sender.sendMessage("/ol enable <script>");
        sender.sendMessage("/ol disable <script>");
        sender.sendMessage("/ol list");
    }
}