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

package com.clubobsidian.obbylang.manager.scheduler;

import com.clubobsidian.crouton.Crouton;
import com.clubobsidian.crouton.wrapper.FutureJobWrapper;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.inject.Inject;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

public class SchedulerManager implements RegisteredManager {

    private final Crouton crouton = new Crouton();
    private final Map<String, Collection<WeakReference<SchedulerJob>>> jobs = new ConcurrentHashMap<>();
    private final FakeServerManager fakeServer;
    private final ScriptManager scriptManager;

    @Inject
    private SchedulerManager(FakeServerManager fakeServer, ScriptManager scriptManager) {
        this.fakeServer = fakeServer;
        this.scriptManager = scriptManager;
    }

    public SchedulerJob sync(final String declaringClass,
                                      final ScriptObjectMirror script) {
        this.init(declaringClass);
        SchedulerJob schedulerJob = this.fakeServer.sync(() -> this.callScript(declaringClass, script));
        this.jobs.get(declaringClass).add(new WeakReference<>(schedulerJob));
        return schedulerJob;
    }

    public SchedulerJob syncDelayed(final String declaringClass,
                                             final ScriptObjectMirror script,
                                             long delay) {
        this.init(declaringClass);
        SchedulerJob schedulerJob = this.fakeServer.syncDelayed(() -> this.callScript(declaringClass, script), delay);
        this.jobs.get(declaringClass).add(new WeakReference<>(schedulerJob));
        return schedulerJob;
    }


    public SchedulerJob syncRepeating(final String declaringClass,
                                               final ScriptObjectMirror script,
                                               long delayStart,
                                               long delayRepeating) {
        this.init(declaringClass);
        SchedulerJob schedulerJob = this.fakeServer
                .scheduleSyncRepeatingTask(() -> this.callScript(declaringClass, script), delayStart, delayRepeating);
        this.jobs.get(declaringClass).add(new WeakReference<>(schedulerJob));
        return schedulerJob;
    }

    public SchedulerJob async(final String declaringClass, final ScriptObjectMirror script) {
        this.init(declaringClass);
        SchedulerJob schedulerJob = new KotlinSchedulerJob(this.crouton.async(() -> {
            this.callScript(declaringClass, script);
        }));
        this.jobs.get(declaringClass).add(new WeakReference<>(schedulerJob));
        return schedulerJob;
    }

    public SchedulerJob asyncDelayed(String declaringClass, final ScriptObjectMirror script, long delay) {
        this.init(declaringClass);
       SchedulerJob schedulerJob = new KotlinSchedulerJob(this.crouton.asyncDelayed(() -> {
            this.callScript(declaringClass, script);
        }, delay));
        this.jobs.get(declaringClass).add(new WeakReference<>(schedulerJob));
        return schedulerJob;
    }

    public SchedulerJob asyncRepeating(final String declaringClass, final ScriptObjectMirror script, long delayStart, long delayRepeating) {
        this.init(declaringClass);
        SchedulerJob schedulerJob = new KotlinSchedulerJob(this.crouton.asyncRepeating(() -> {
            this.callScript(declaringClass, script);
        }, delayStart, delayRepeating));
        this.jobs.get(declaringClass).add(new WeakReference<>(schedulerJob));
        return schedulerJob;
    }

    public Future<Object> await(String declaringClass, final ScriptObjectMirror script) {
        return this.asyncWait(declaringClass, script);
    }

    public Future<Object> asyncWait(final String declaringClass, final ScriptObjectMirror script) {
        this.init(declaringClass);
        FutureJobWrapper wrapper = this.crouton.await(() -> this.callScript(declaringClass, script));
        this.jobs.get(declaringClass).add(new WeakReference<>(new KotlinSchedulerJob(wrapper)));
        return wrapper.getFuture();
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        Iterator<WeakReference<SchedulerJob>> it = this.jobs.get(declaringClass).iterator();
        while(it.hasNext()) {
            WeakReference<SchedulerJob> ref = it.next();
            SchedulerJob job = ref.get();
            if(job != null) {
                job.stop();
            }
            it.remove();
        }
    }

    private Object callScript(String declaringClass, ScriptObjectMirror script) {
        return script.call(this.scriptManager.getScript(declaringClass));
    }

    private void init(String declaringClass) {
        this.jobs.computeIfAbsent(declaringClass, k -> new ConcurrentLinkedQueue<>());
    }
}