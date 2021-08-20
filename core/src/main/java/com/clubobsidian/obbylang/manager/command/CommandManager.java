package com.clubobsidian.obbylang.manager.command;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandManager implements RegisteredManager {

    private static CommandManager instance;

    public static CommandManager get() {
        if(instance == null) {
            instance = ObbyLang.get().getPlugin().getInjector().getInstance(CommandManager.class);
        }
        return instance;
    }

    private Map<String, List<CommandWrapper<?>>> commands;

    public CommandManager() {
        this.commands = new HashMap<>();
    }

    public void register(String declaringClass, Value script, String command) {
        this.register(declaringClass, script, new String[]{command});
    }

    public void register(String declaringClass, Value script, String... cmds) {
        for(String command : cmds) {
            command = command.toLowerCase();

            List<CommandWrapper<?>> commands = this.commands.get(declaringClass);
            if(commands == null) {
                commands = new ArrayList<>();
                this.commands.put(declaringClass, commands);
            }

            CommandWrapper<?> wrapper = CommandWrapperManager.get().createCommandWrapper(declaringClass, command, script);
            commands.add(wrapper);
            this.removeCommand(wrapper);
            this.registerCommand(wrapper);
        }
    }

    public void unregister(String declaringClass) {
        List<CommandWrapper<?>> commands = this.commands.get(declaringClass);
        if(commands != null) {
            for(CommandWrapper<?> wrapper : commands) {
                this.removeCommand(wrapper);
            }
        }
        this.commands.keySet().remove(declaringClass);
    }

    protected Map<String, List<CommandWrapper<?>>> getCommands() {
        return this.commands;
    }

    protected abstract boolean registerCommand(CommandWrapper<?> wrapper);

    protected abstract boolean removeCommand(CommandWrapper<?> wrapper);

}