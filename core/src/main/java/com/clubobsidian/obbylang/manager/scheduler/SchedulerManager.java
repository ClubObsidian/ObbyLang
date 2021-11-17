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

package com.clubobsidian.obbylang.manager.scheduler;

import com.clubobsidian.crouton.Crouton;
import com.clubobsidian.crouton.wrapper.FutureJobWrapper;
import com.clubobsidian.crouton.wrapper.JobWrapper;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.google.inject.Inject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

public class SchedulerManager implements RegisteredManager {

    private final Crouton crouton = new Crouton();
    private final Map<String, Collection<WeakReference<JobWrapper>>> jobs = new ConcurrentHashMap<>();
    private final Queue<Runnable> syncQueue = new ConcurrentLinkedQueue<>();

    private final FakeServerManager fakeServer;
    private final ScriptManager scriptManager;

    @Inject
    private SchedulerManager(FakeServerManager fakeServer, ScriptManager scriptManager) {
        this.fakeServer = fakeServer;
        this.scriptManager = scriptManager;
        this.startSyncQueueTask();
    }

    private void startSyncQueueTask() {
        this.fakeServer.scheduleSyncRepeatingTask(() -> {
            Runnable runnable;
            while((runnable = this.syncQueue.poll()) != null) {
                runnable.run();
            }
        }, 1, 1);
    }

    public synchronized void sync(final String declaringClass, final ScriptObjectMirror script) {
        this.syncQueue.add(() -> this.callScript(declaringClass, script));
    }

    public JobWrapper syncDelayed(String declaringClass, final ScriptObjectMirror script, long delay) {
        this.init(declaringClass);
        JobWrapper wrapper = this.crouton.asyncDelayed(() -> {
            this.syncQueue.add(() -> {
                this.callScript(declaringClass, script);
            });
        }, delay);
        return wrapper;
    }


    public JobWrapper syncRepeating(String declaringClass, final ScriptObjectMirror script, long delayStart, long delayRepeating) {
        this.init(declaringClass);
        JobWrapper wrapper = this.crouton.asyncRepeating(() -> {
            this.syncQueue.add(() -> {
                this.callScript(declaringClass, script);
            });
        }, delayStart, delayRepeating);
        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public JobWrapper async(final String declaringClass, final ScriptObjectMirror script) {
        this.init(declaringClass);
        JobWrapper wrapper = this.crouton.async(() -> {
            this.callScript(declaringClass, script);
        });
        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public JobWrapper asyncRepeating(final String declaringClass, final ScriptObjectMirror script, long delayStart, long delayRepeating) {
        this.init(declaringClass);
        JobWrapper wrapper = this.crouton.asyncRepeating(() -> {
            this.callScript(declaringClass, script);
        }, delayStart, delayRepeating);
        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public Future<Object> await(String declaringClass, final ScriptObjectMirror script) {
        return this.asyncWait(declaringClass, script);
    }

    public Future<Object> asyncWait(final String declaringClass, final ScriptObjectMirror script) {
        this.init(declaringClass);
        FutureJobWrapper wrapper = this.crouton.await(() -> this.callScript(declaringClass, script));
        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper.getFuture();
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        Iterator<WeakReference<JobWrapper>> it = this.jobs.get(declaringClass).iterator();
        while(it.hasNext()) {
            WeakReference<JobWrapper> ref = it.next();
            JobWrapper wrapper = ref.get();
            if(wrapper != null) {
                wrapper.stop();
            }
            it.remove();
        }
    }

    private Object callScript(String declaringClass, ScriptObjectMirror script) {
        return script.call(this.scriptManager.getScript(declaringClass));
    }

    private void init(String declaringClass) {
        if(this.jobs.get(declaringClass) == null) {
            this.jobs.put(declaringClass, new ConcurrentLinkedQueue<>());
        }
    }
}