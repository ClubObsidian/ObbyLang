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

package com.clubobsidian.obbylang.inject;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.clubobsidian.obbylang.manager.database.DatabaseManager;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.global.GlobalManager;
import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.clubobsidian.obbylang.manager.message.MessageManager;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import com.clubobsidian.obbylang.manager.redis.RedisManager;
import com.clubobsidian.obbylang.manager.script.DisableManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.trident.EventBus;
import com.clubobsidian.trident.eventbus.methodhandle.MethodHandleEventBus;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class PluginInjector {

    private ObbyLangPlugin plugin;
    private Class<? extends ProxyManager<?>> proxyManager;
    private Class<? extends MessageManager<?>> messageManager;
    private Class<? extends CustomEventManager> customEventManager;
    private Class<? extends DependencyManager> dependencyManager;
    private Class<? extends FakeServerManager> fakeServerManager;
    private Class<? extends ListenerManager<?>> listenerManager;
    private Class<? extends CommandManager> commandManager;
    private Class<? extends CommandWrapperManager<?>> commandWrapperManager;
    private final Collection<Module> addonModules = new ArrayList<>();

    public PluginInjector injectPlugin(ObbyLangPlugin plugin) {
        /*Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());
        PluginModule pluginModule = new PluginModule(plugin);
        Guice.createInjector(pluginModule);*/
        this.plugin = plugin;
        return this;
    }

    public PluginInjector setProxyManager(Class<? extends ProxyManager<?>> proxyManager) {
        this.proxyManager = proxyManager;
        return this;
    }

    public PluginInjector setMessageManager(Class<? extends MessageManager<?>> messageManager) {
        this.messageManager = messageManager;
        return this;
    }

    public PluginInjector setCustomEventManager(Class<? extends CustomEventManager> customEventManager) {
        this.customEventManager = customEventManager;
        return this;
    }

    public PluginInjector setDependencyManager(Class<? extends DependencyManager> dependencyManager) {
        this.dependencyManager = dependencyManager;
        return this;
    }

    public PluginInjector setFakeServerManager(Class<? extends FakeServerManager> fakeServerManager) {
        this.fakeServerManager = fakeServerManager;
        return this;
    }

    public PluginInjector setListenerManager(Class<? extends ListenerManager<?>> listenerManager) {
        this.listenerManager = listenerManager;
        return this;
    }

    public PluginInjector setCommandManager(Class<? extends CommandManager> commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public PluginInjector setCommandWrapperManager(Class<? extends CommandWrapperManager<?>> commandWrapperManager) {
        this.commandWrapperManager = commandWrapperManager;
        return this;
    }

    public <T> PluginInjector addAddon(Class<T>  bind) {
        return this.addAddon(bind, bind);
    }
    public <T> PluginInjector addAddon(Class<T> bindFrom, Class<? extends T> bindTo) {
        this.addonModules.add(new AddonModule(bindFrom, bindTo));
        return this;
    }

    public ObbyLang create() {
        Collection<Module> toInject = new ArrayList<>();
        toInject.add(new BuiltinModule());
        toInject.add(new DependencyModule());
        toInject.addAll(this.addonModules);
        Injector dependencyInjector = Guice.createInjector(toInject);
        Injector obbyLangInjector = Guice.createInjector(new ObbyLangModule(dependencyInjector));
        return obbyLangInjector.getInstance(ObbyLang.class);
    }

    private class AddonModule<T> implements Module {

        private final Class<T> bindFrom;
        private final Class<? extends T> bindTo;

        public AddonModule(Class<T> bindFrom, Class<? extends T> bindTo) {
            this.bindFrom = bindFrom;
            this.bindTo = bindTo;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(this.bindFrom).to(this.bindTo).asEagerSingleton();
        }
    }

    private class ObbyLangModule implements Module {

        private final Injector injector;

        public ObbyLangModule(Injector injector) {
            this.injector = injector;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(ObbyLangPlugin.class).toInstance(plugin);
            binder.bind(Injector.class).toInstance(this.injector);
            binder.bind(ObbyLang.class).to(ObbyLang.class).asEagerSingleton();
        }
    }

    private class BuiltinModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(ObbyLangPlugin.class).toInstance(plugin);
            binder.bind(EventBus.class).toInstance(new MethodHandleEventBus());
            binder.bind(GlobalManager.class).to(GlobalManager.class).asEagerSingleton();
            binder.bind(MappingsManager.class).to(MappingsManager.class).asEagerSingleton();
            binder.bind(DatabaseManager.class).to(DatabaseManager.class).asEagerSingleton();
            binder.bind(AddonManager.class).to(AddonManager.class).asEagerSingleton();
            binder.bind(ScriptManager.class).to(ScriptManager.class).asEagerSingleton();
            binder.bind(RedisManager.class).to(RedisManager.class).asEagerSingleton();
            binder.bind(DisableManager.class).to(DisableManager.class).asEagerSingleton();
        }
    }

    private class DependencyModule implements Module {
        @Override
        public void configure(Binder binder) {
            new InjectorBinder<FakeServerManager>().bind(binder, FakeServerManager.class, fakeServerManager);
            new InjectorBinder<ProxyManager>().bind(binder, ProxyManager.class, proxyManager);
            new InjectorBinder<MessageManager>().bind(binder, MessageManager.class, messageManager);
            new InjectorBinder<CustomEventManager>().bind(binder, CustomEventManager.class, customEventManager);
            new InjectorBinder<DependencyManager>().bind(binder, DependencyManager.class, dependencyManager);
            new InjectorBinder<ListenerManager>().bind(binder, ListenerManager.class, listenerManager);
            new InjectorBinder<CommandWrapperManager>().bind(binder, CommandWrapperManager.class, commandWrapperManager);
            new InjectorBinder<CommandManager>().bind(binder, CommandManager.class, commandManager);
        }
    }

    private class InjectorBinder<T> {
        @SuppressWarnings("unchecked")
        public void bind(Binder binder, Class<T> clazz, Class<? extends T> bind) {
            if(bind != null) {
                binder.bind(clazz).to(bind).asEagerSingleton();
            } else {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    pool.appendClassPath(new ClassClassPath(clazz));
                    CtClass managerClass = pool.get(clazz.getName());
                    CtClass newManagerCtClass = pool.makeClass("Template" + clazz.getName());
                    newManagerCtClass.setSuperclass(managerClass);
                    newManagerCtClass.setModifiers(Modifier.PUBLIC);
                    for(CtMethod m : newManagerCtClass.getMethods()) {
                        if((m.getModifiers() & Modifier.ABSTRACT) != 0) {
                            CtClass returnType = m.getReturnType();
                            String name = m.getName();
                            CtClass[] parameters = m.getParameterTypes();
                            CtClass[] exceptions = m.getExceptionTypes();
                            String body = null;
                            CtMethod newMethod = CtNewMethod.make(returnType, name, parameters, exceptions, body, newManagerCtClass);
                            newManagerCtClass.addMethod(newMethod);
                        }
                    }
                    Class<? extends T> newManagerClass = (Class<? extends T>) newManagerCtClass.toClass();
                    binder.bind(clazz).to(newManagerClass).asEagerSingleton();
                } catch(CannotCompileException | NotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}