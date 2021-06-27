package com.clubobsidian.obbylang.plugin;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.ObbyLangPlatform;
import com.clubobsidian.obbylang.command.ObbyLangCommand;
import com.clubobsidian.obbylang.command.ObbyLangCommandTabCompleter;
import com.clubobsidian.obbylang.guice.PluginInjector;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitCustomEventManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitDependencyManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitFakeServerManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitListenerManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitMessageManager;
import com.clubobsidian.obbylang.manager.bukkit.BukkitProxyManager;
import com.clubobsidian.obbylang.manager.bukkit.command.BukkitCommandManager;
import com.clubobsidian.obbylang.manager.bukkit.command.BukkitCommandWrapperManager;
import com.clubobsidian.obbylang.manager.bukkit.world.InstanceManager;
import com.clubobsidian.obbylang.manager.plugin.PluginManager;
import com.google.inject.Injector;
import javassist.ClassClassPath;
import javassist.ClassPool;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public class BukkitObbyLangPlugin extends JavaPlugin implements ObbyLangPlugin, Listener {

    private static BukkitObbyLangPlugin instance;

    private Injector injector;
    private CommandManager<CommandSender> commandManager;
    private AnnotationParser<CommandSender> commandParser;

    @Override
    public boolean createObbyLangCommand() {
        this.commandManager = this.createCommandManager();
        this.commandParser = new AnnotationParser(this.commandManager,
                CommandSender.class, parameters ->
                SimpleCommandMeta.empty());
        this.commandParser.parse(new ObbyLangCommand());
        //this.getCommand("obbylang").setExecutor(new ObbyLangCommand());
        //this.getCommand("obbylang").setTabCompleter(new ObbyLangCommandTabCompleter());
        return true;
    }

    @Override
    public ObbyLangPlatform getPlatform() {
        return ObbyLangPlatform.SPIGOT;
    }

    @Override
    public Injector getInjector() {
        return this.injector;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("Injecting obbylang plugin");
        this.injector = new PluginInjector()
                .injectPlugin(this)
                .setProxyManager(new BukkitProxyManager())
                .setMessageManager(new BukkitMessageManager())
                .setCustomEventManager(new BukkitCustomEventManager())
                .setDependencyManager(new BukkitDependencyManager())
                .setFakeServerManager(new BukkitFakeServerManager())
                .setListenerManager(new BukkitListenerManager())
                .setCommandManager(new BukkitCommandManager())
                .setCommandWrapperManager(new BukkitCommandWrapperManager())
                .create();

        PluginManager.get(); //Initialize plugin manager
        AddonManager.get().registerAddon("instanceManager", InstanceManager.get());

        ClassPool.getDefault().insertClassPath(new ClassClassPath(Listener.class));
        this.getLogger().info("About to enable obbylang");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        ObbyLang.get().onEnable();
    }

    @Override
    public void onDisable() {
        ObbyLang.get().onDisable();
    }

    public static BukkitObbyLangPlugin get() {
        return instance;
    }

    private CommandManager<CommandSender> createCommandManager() {
        try {
            PaperCommandManager<CommandSender> commandManager = new PaperCommandManager(this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity());
            if (commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
                commandManager.registerBrigadier();
            }
            return commandManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}