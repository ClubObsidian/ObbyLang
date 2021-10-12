package com.clubobsidian.obbylang.manager.plugin;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.event.PluginEnableEvent;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.trident.EventHandler;
import com.clubobsidian.trident.EventPriority;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class DependencyManager implements RegisteredManager {

    private static DependencyManager instance;

    public static DependencyManager get() {
        if(instance == null) {
            instance = ObbyLang.get().getPlugin().getInjector().getInstance(DependencyManager.class);
        }
        return instance;
    }

    private final Map<String, Queue<DependencyWrapper>> dependencies = new ConcurrentHashMap<>();

    public DependencyManager() {
        ObbyLang.get().getEventBus().registerEvents(this);
        this.registerPluginEnableListener();
    }

    private void init(String declaringClass) {
        if(this.dependencies.get(declaringClass) == null) {
            this.dependencies.put(declaringClass, new ConcurrentLinkedQueue<>());
        }
    }

    public void register(String declaringClass, ScriptObjectMirror script, String dependency) {
        this.register(declaringClass, script, new String[]{dependency});
    }

    public void register(String declaringClass, ScriptObjectMirror script, String[] dependencies) {
        this.init(declaringClass);
        DependencyWrapper wrapper = new DependencyWrapper(script, dependencies);
        boolean hasDependencies = this.checkDependencies(declaringClass, wrapper);
        if(hasDependencies) {
            script.call(ScriptManager.get().getScript(declaringClass));
        } else {
            this.dependencies.get(declaringClass).add(wrapper);
        }
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        this.dependencies.keySet().remove(declaringClass);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginEnable(PluginEnableEvent e) {
        Iterator<Entry<String, Queue<DependencyWrapper>>> it = this.dependencies.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Queue<DependencyWrapper>> next = it.next();
            Queue<DependencyWrapper> queue = next.getValue();
            Iterator<DependencyWrapper> listIterator = queue.iterator();
            while(listIterator.hasNext()) {
                DependencyWrapper wrapper = listIterator.next();
                String declaringClass = next.getKey();
                boolean hasDependencies = this.checkDependencies(declaringClass, wrapper);
                if(hasDependencies) {
                    wrapper.getScript().call(ScriptManager.get().getScript(declaringClass));
                    listIterator.remove();
                }
            }
            if(queue.size() == 0) {
                it.remove();
            }
        }
    }

    private boolean checkDependencies(String declaringClass, DependencyWrapper wrapper) {
        for(String dependency : wrapper.getDependencies()) {
            if(FakeServerManager.get().getPlugin(dependency) == null) {
                return false;
            }
        }
        return true;
    }

    public abstract void registerPluginEnableListener();
}