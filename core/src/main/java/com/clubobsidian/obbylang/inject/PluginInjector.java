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
import com.google.inject.TypeLiteral;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.attribute.AnnotationRetention;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

public class PluginInjector {

    private ClassLoader loader;
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
        this.loader = plugin.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(this.loader);
        /*PluginModule pluginModule = new PluginModule(plugin);
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
        this.addonModules.add(new SameClassAddonModule<>(bind));
        return this;
    }
    public <T> PluginInjector addAddon(Class<T> bindFrom, Class<? extends T> bindTo) {
        this.addonModules.add(new DifferentClassAddonModule<>(bindFrom, bindTo));
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

    private class SameClassAddonModule<T> implements Module {

        private final Class<T> bind;

        public SameClassAddonModule(Class<T> bind) {
            this.bind = bind;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(this.bind).asEagerSingleton();
        }
    }

    private class DifferentClassAddonModule<T> implements Module {

        private final Class<T> bindFrom;
        private final Class<? extends T> bindTo;

        public DifferentClassAddonModule(Class<T> bindFrom, Class<? extends T> bindTo) {
            this.bindFrom = bindFrom;
            this.bindTo = bindTo;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(this.bindFrom).to(this.bindTo).asEagerSingleton();
        }
    }

    private class ObbyLangModule implements Module {

        private final InjectorWrapper wrapper;

        public ObbyLangModule(Injector injector) {
            this.wrapper = new InjectorWrapper(injector);
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(ObbyLangPlugin.class).toInstance(plugin);
            binder.bind(InjectorWrapper.class).toInstance(this.wrapper);
            binder.bind(ObbyLang.class).asEagerSingleton();
        }
    }

    private class BuiltinModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(ObbyLangPlugin.class).toInstance(plugin);
            binder.bind(EventBus.class).toInstance(new MethodHandleEventBus());
            binder.bind(GlobalManager.class).asEagerSingleton();
            binder.bind(MappingsManager.class).asEagerSingleton();
            binder.bind(DatabaseManager.class).asEagerSingleton();
            binder.bind(AddonManager.class).asEagerSingleton();
            binder.bind(ScriptManager.class).asEagerSingleton();
            binder.bind(RedisManager.class).asEagerSingleton();
            binder.bind(DisableManager.class).asEagerSingleton();
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
            new InjectorBinder<CommandWrapperManager<?>>().bind(binder, new TypeLiteral<>() {}, commandWrapperManager);
            new InjectorBinder<CommandManager>().bind(binder, CommandManager.class, commandManager);
        }
    }

    private class InjectorBinder<T> {
        @SuppressWarnings("unchecked")
        public void bind(Binder binder, Class<T> clazz, Class<? extends T> bind) {
            this.bind(binder, TypeLiteral.get(clazz), bind);
        }
        public void bind(Binder binder, TypeLiteral<T> bindTo, Class<? extends T> bind) {
            if(bind != null) {
                binder.bind(bindTo).to(bind).asEagerSingleton();
            } else {
                Class<T> clazz = (Class<T>) bindTo.getRawType();
                Class<? extends T> newManagerClazz = new ByteBuddy()
                        .subclass(clazz, ConstructorStrategy.Default.IMITATE_SUPER_CLASS.withInheritedAnnotations())
                        .method(ElementMatchers.isAbstract())
                        .intercept(MethodDelegation.to(ProxyInterceptor.class))
                        .make()
                        .load(loader)
                        .getLoaded();
                binder.bind(bindTo).to(newManagerClazz).asEagerSingleton();
            }
        }
    }
}