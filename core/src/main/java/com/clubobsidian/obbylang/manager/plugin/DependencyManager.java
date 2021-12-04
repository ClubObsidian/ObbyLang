/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.manager.plugin;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.event.PluginEnableEvent;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.trident.EventBus;
import com.clubobsidian.trident.EventHandler;
import com.clubobsidian.trident.EventPriority;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class DependencyManager implements RegisteredManager {

    private final Map<String, Queue<DependencyWrapper>> dependencies = new ConcurrentHashMap<>();
    private final ScriptManager scriptManager;
    private final EventBus eventBus;
    private final FakeServerManager fakeServer;

    @Inject
    protected DependencyManager(EventBus eventBus, ScriptManager scriptManager, FakeServerManager fakeServer) {
        this.eventBus = eventBus;
        this.scriptManager = scriptManager;
        this.fakeServer = fakeServer;
        this.eventBus.registerEvents(this);
        this.registerPluginEnableListener();
    }

    private void init(String declaringClass) {
        if(this.dependencies.get(declaringClass) == null) {
            this.dependencies.put(declaringClass, new ConcurrentLinkedQueue<>());
        }
    }

    protected EventBus getEventBus() {
        return this.eventBus;
    }

    public void register(String declaringClass, ScriptObjectMirror script, String dependency) {
        this.register(declaringClass, script, new String[]{dependency});
    }

    public void register(String declaringClass, ScriptObjectMirror script, String[] dependencies) {
        this.init(declaringClass);
        DependencyWrapper wrapper = new DependencyWrapper(script, dependencies);
        boolean hasDependencies = this.checkDependencies(declaringClass, wrapper);
        if(hasDependencies) {
            script.call(this.scriptManager.getScript(declaringClass));
        } else {
            this.dependencies.get(declaringClass).add(wrapper);
        }
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        this.dependencies.keySet().remove(declaringClass);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginEnable(PluginEnableEvent event) {
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
                    wrapper.getScript().call(this.scriptManager.getScript(declaringClass));
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
            if(this.fakeServer.getPlugin(dependency) == null) {
                return false;
            }
        }
        return true;
    }

    public abstract void registerPluginEnableListener();
}