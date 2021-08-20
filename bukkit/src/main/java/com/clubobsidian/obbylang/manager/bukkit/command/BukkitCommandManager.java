package com.clubobsidian.obbylang.manager.bukkit.command;

import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapper;
import com.clubobsidian.obbylang.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.graalvm.polyglot.Value;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class BukkitCommandManager extends CommandManager {

    private CommandMap cm = null;

    @SuppressWarnings("unchecked")
    private Map<String, Command> getKnownCommands() {
        try {
            Field commandField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            commandField.setAccessible(true);
            return (Map<String, Command>) commandField.get(this.getCommandMap());
        } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final CommandMap getCommandMap() {
        if(this.cm == null) {
            try {
                Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                this.cm = (CommandMap) f.get(Bukkit.getServer());
            } catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return this.cm;
    }

    public Command getCommand(String commandName) {
        return this.getKnownCommands().get(commandName);
    }

    public BukkitTabCompleter createTabCompleter(String owner, Value script) {
        return new BukkitTabCompleter(owner, script);
    }

    @Override
    protected boolean registerCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            Command cmd = (Command) wrapper.getCommand();
            this.removeCommand(wrapper);
            this.getCommandMap().register("", cmd);
            if(this.useBrigadier()) {
                String commandName = wrapper.getCommandName();
                this.unregisterBrigadier(commandName);
                this.registerBrigadier(cmd, commandName);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean removeCommand(CommandWrapper<?> wrapper) {
        if(wrapper.getCommand() instanceof Command) {
            String commandName = wrapper.getCommandName();
            this.getKnownCommands().keySet().remove(commandName);
            this.getKnownCommands().remove(commandName);
            if(this.useBrigadier()) {
                this.unregisterBrigadier(commandName);
            }
        }
        return false;
    }

    private boolean useBrigadier() {
        try {
            ReflectionUtil.getCraftClass("command", "BukkitCommandWrapper");
            return true;
        } catch(ClassNotFoundException ex) {
            return false;
        }
    }

    private void registerBrigadier(Command cmd, String label) {
        try {
            Class<?> craftServerClass = ReflectionUtil.getCraftClass("CraftServer");
            Object craftServer = Bukkit.getServer();
            Field consoleField = craftServerClass.getDeclaredField("console");
            consoleField.setAccessible(true);
            Class<?> consoleClass = ReflectionUtil.getMinecraftClass("MinecraftServer");
            Object console = consoleField.get(craftServer);
            Field dispatcherField = null;
            if(this.fieldExists(consoleClass, "commandDispatcher")) {
                dispatcherField = consoleClass.getDeclaredField("commandDispatcher");
            } else {
                dispatcherField = consoleClass.getDeclaredField("vanillaCommandDispatcher");
            }
            dispatcherField.setAccessible(true);
            Object dispatcher = dispatcherField.get(console);

            Class<?> nmsDispatcherClass = ReflectionUtil.getMinecraftClass("CommandDispatcher");
            Method toBrigadierDispatcher = nmsDispatcherClass.getDeclaredMethod("a");

            Class<?> bukkitCommandWrapperClass = ReflectionUtil.getCraftClass("command", "BukkitCommandWrapper");
            Constructor<?> con = bukkitCommandWrapperClass.getDeclaredConstructor(craftServerClass, Command.class);
            Object wrapper = con.newInstance(craftServer, cmd);

            Class<?> brigadierDispatcherClass = Class.forName("com.mojang.brigadier.CommandDispatcher");
            Method register = bukkitCommandWrapperClass.getDeclaredMethod("register", brigadierDispatcherClass, String.class);
            Object brigadierDispatcher = toBrigadierDispatcher.invoke(dispatcher);
            register.invoke(wrapper, brigadierDispatcher, label);
        } catch(ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void unregisterBrigadier(String label) {
        try {
            Class<?> craftServerClass = ReflectionUtil.getCraftClass("CraftServer");
            Object craftServer = Bukkit.getServer();
            Field consoleField = craftServerClass.getDeclaredField("console");
            consoleField.setAccessible(true);
            Class<?> consoleClass = ReflectionUtil.getMinecraftClass("MinecraftServer");
            Object console = consoleField.get(craftServer);
            Field dispatcherField = null;
            if(this.fieldExists(consoleClass, "commandDispatcher")) {
                dispatcherField = consoleClass.getDeclaredField("commandDispatcher");
            } else {
                dispatcherField = consoleClass.getDeclaredField("vanillaCommandDispatcher");
            }
            dispatcherField.setAccessible(true);
            Object dispatcher = dispatcherField.get(console);

            Class<?> nmsDispatcherClass = ReflectionUtil.getMinecraftClass("CommandDispatcher");
            Method toBrigadierDispatcher = nmsDispatcherClass.getDeclaredMethod("a");

            Class<?> brigadierDispatcherClass = Class.forName("com.mojang.brigadier.CommandDispatcher");
            Object brigadierDispatcher = toBrigadierDispatcher.invoke(dispatcher);

            Method getRootMethod = brigadierDispatcherClass.getDeclaredMethod("getRoot");
            Object root = getRootMethod.invoke(brigadierDispatcher);

            Class<?> commandNodeClass = Class.forName("com.mojang.brigadier.tree.CommandNode");
            Method removeCommandMethod = commandNodeClass.getDeclaredMethod("removeCommand", String.class);
            removeCommandMethod.invoke(root, label);
        } catch(ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private boolean fieldExists(Class<?> clazz, String fieldName) {
        for(Field field : clazz.getDeclaredFields()) {
            if(field.getName().equals(fieldName)) {
                return true;
            }
        }

        return false;
    }
}