package com.clubobsidian.obbylang.bungeecord.manager.command;

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.Map;

public class BungeeCordCommandManager extends CommandManager {

    private Map<String, Command> cm = null;

    @SuppressWarnings("unchecked")
    private final Map<String, Command> getCommandMap() {
        if(this.cm == null) {
            try {
                PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
                Field f = pluginManager.getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                this.cm = (Map<String, Command>) f.get(pluginManager);
            } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return this.cm;
    }

    @Override
    protected boolean registerCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            Command cmd = (Command) wrapper.getCommand();
            this.removeCommand(wrapper);
            this.getCommandMap().put(cmd.getName(), cmd);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removeCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            String commandName = wrapper.getCommandName();
            this.getCommandMap().remove(commandName);
        }
        return false;
    }
}