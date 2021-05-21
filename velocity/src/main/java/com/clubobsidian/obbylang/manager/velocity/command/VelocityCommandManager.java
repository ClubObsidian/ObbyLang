package com.clubobsidian.obbylang.manager.velocity.command;

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.plugin.VelocityObbyLangPlugin;
import com.velocitypowered.api.command.Command;

import java.lang.reflect.Field;
import java.util.Map;

public class VelocityCommandManager extends CommandManager {

    private Map<String, Command> cm = null;

    @SuppressWarnings("unchecked")
    private final Map<String, Command> getCommandMap() {
        if(this.cm == null) {
            try {
                com.velocitypowered.api.command.CommandManager pluginManager = VelocityObbyLangPlugin
                        .get()
                        .getServer()
                        .getCommandManager();
                Field f = pluginManager.getClass().getDeclaredField("commands");
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
            this.getCommandMap().put(wrapper.getCommandName(), cmd);
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