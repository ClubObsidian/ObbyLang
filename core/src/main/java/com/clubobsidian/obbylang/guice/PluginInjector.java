package com.clubobsidian.obbylang.guice;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.command.CommandManager;
import com.clubobsidian.obbylang.manager.command.CommandWrapperManager;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.clubobsidian.obbylang.manager.message.MessageManager;
import com.clubobsidian.obbylang.manager.plugin.DependencyManager;
import com.clubobsidian.obbylang.manager.proxy.ProxyManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
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

public class PluginInjector {

    private ProxyManager<?> proxyManager;
    private MessageManager<?> messageManager;
    private CustomEventManager customEventManager;
    private DependencyManager dependencyManager;
    private FakeServerManager fakeServerManager;
    private ListenerManager<?> listenerManager;
    private CommandManager commandManager;
    private CommandWrapperManager<?> commandWrapperManager;

    public PluginInjector injectPlugin(ObbyLangPlugin plugin) {
        Thread.currentThread().setContextClassLoader(plugin.getClass().getClassLoader());
        PluginModule pluginModule = new PluginModule(plugin);
        Guice.createInjector(pluginModule);
        return this;
    }

    public PluginInjector setProxyManager(ProxyManager<?> proxyManager) {
        this.proxyManager = proxyManager;
        return this;
    }

    public PluginInjector setMessageManager(MessageManager<?> messageManager) {
        this.messageManager = messageManager;
        return this;
    }

    public PluginInjector setCustomEventManager(CustomEventManager customEventManager) {
        this.customEventManager = customEventManager;
        return this;
    }

    public PluginInjector setDependencyManager(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
        return this;
    }

    public PluginInjector setFakeServerManager(FakeServerManager fakeServerManager) {
        this.fakeServerManager = fakeServerManager;
        return this;
    }

    public PluginInjector setListenerManager(ListenerManager<?> listenerManager) {
        this.listenerManager = listenerManager;
        return this;
    }

    public PluginInjector setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        return this;
    }

    public PluginInjector setCommandWrapperManager(CommandWrapperManager<?> commandWrapperManager) {
        this.commandWrapperManager = commandWrapperManager;
        return this;
    }

    public Injector create() {
        return Guice.createInjector(new DependencyModule());
    }

    private class PluginModule implements Module {

        private final ObbyLangPlugin plugin;

        public PluginModule(ObbyLangPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void configure(Binder binder) {
            new InjectorBinder<ObbyLangPlugin>()
                    .bind(binder, ObbyLangPlugin.class, this.plugin);

            new InjectorBinder<ObbyLang>()
                    .bind(binder, ObbyLang.class, ObbyLang.get());
        }
    }


    private class DependencyModule implements Module {

        @SuppressWarnings("rawtypes")
        @Override
        public void configure(Binder binder) {
            new InjectorBinder<ProxyManager>()
                    .bind(binder, ProxyManager.class, proxyManager);

            new InjectorBinder<MessageManager>()
                    .bind(binder, MessageManager.class, messageManager);

            new InjectorBinder<CustomEventManager>()
                    .bind(binder, CustomEventManager.class, customEventManager);

            new InjectorBinder<DependencyManager>()
                    .bind(binder, DependencyManager.class, dependencyManager);

            new InjectorBinder<FakeServerManager>()
                    .bind(binder, FakeServerManager.class, fakeServerManager);

            new InjectorBinder<ListenerManager>()
                    .bind(binder, ListenerManager.class, listenerManager);

            new InjectorBinder<CommandManager>()
                    .bind(binder, CommandManager.class, commandManager);

            new InjectorBinder<CommandWrapperManager>()
                    .bind(binder, CommandWrapperManager.class, commandWrapperManager);

        }

    }

    private class InjectorBinder<T> {
        @SuppressWarnings("unchecked")
        public void bind(Binder binder, Class<T> clazz, T bind) {
            if(bind != null) {
                binder.bind(clazz)
                        .toInstance(bind);
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

                    Class<?> newManagerClass = newManagerCtClass.toClass();

                    T factoryObject = (T) newManagerClass.newInstance();
                    binder.bind(clazz)
                            .toInstance(factoryObject);
                } catch(CannotCompileException | NotFoundException | InstantiationException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }
}