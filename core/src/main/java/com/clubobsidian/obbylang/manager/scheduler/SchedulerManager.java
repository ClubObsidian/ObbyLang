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
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

public class SchedulerManager implements RegisteredManager {

    private static SchedulerManager instance;

    public static SchedulerManager get() {
        if(instance == null) {
            instance = new SchedulerManager();
        }
        return instance;
    }

    private final Crouton crouton = new Crouton();
    private final Map<String, ConcurrentLinkedQueue<WeakReference<JobWrapper>>> jobs = new ConcurrentHashMap<>();
    private final Queue<Runnable> syncQueue = new ConcurrentLinkedQueue<>();

    private SchedulerManager() {
        this.startSyncQueueTask();
    }

    private void startSyncQueueTask() {
        FakeServerManager.get().scheduleSyncRepeatingTask(() ->
        {
            Runnable runnable = null;
            while((runnable = syncQueue.poll()) != null) {
                runnable.run();
            }
        }, 1, 1);
    }

    public synchronized void sync(final String declaringClass, final ScriptObjectMirror script) {
        this.syncQueue.add(() ->
        {
            script.call(ScriptManager.get().getScript(declaringClass));
        });
    }

    public JobWrapper syncDelayed(String declaringClass, final ScriptObjectMirror script, Long delay) {
        this.init(declaringClass);

        JobWrapper wrapper = this.crouton.asyncDelayed(() ->
        {
            this.syncQueue.add(() ->
            {
                script.call(ScriptManager.get().getScript(declaringClass));
            });
        }, delay);

        return wrapper;
    }


    public JobWrapper syncRepeating(String declaringClass, final ScriptObjectMirror script, Long delayStart, Long delayRepeating) {
        this.init(declaringClass);

        JobWrapper wrapper = this.crouton.asyncRepeating(() ->
        {
            this.syncQueue.add(() ->
            {
                script.call(ScriptManager.get().getScript(declaringClass));
            });
        }, delayStart, delayRepeating);

        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public JobWrapper async(final String declaringClass, final ScriptObjectMirror script) {
        this.init(declaringClass);

        JobWrapper wrapper = this.crouton.async(() ->
        {
            script.call(ScriptManager.get().getScript(declaringClass));
        });

        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public JobWrapper asyncRepeating(final String declaringClass, final ScriptObjectMirror script, Long delayStart, Long delayRepeating) {
        this.init(declaringClass);

        JobWrapper wrapper = this.crouton.asyncRepeating(() ->
        {
            script.call(ScriptManager.get().getScript(declaringClass));
        }, delayStart, delayRepeating);

        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public Future<Object> await(String declaringClass, final ScriptObjectMirror script) {
        return this.asyncWait(declaringClass, script);
    }

    public Future<Object> asyncWait(final String declaringClass, final ScriptObjectMirror script) {
        this.init(declaringClass);

        FutureJobWrapper wrapper = this.crouton.await(() -> script.call(ScriptManager.get().getScript(declaringClass)));
        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper.getFuture();
    }

    public void sleep(Long delay) {
        this.delay(delay);
    }

    public void delay(Long delay) {
        this.crouton.delay(delay);
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

    private void init(String declaringClass) {
        if(this.jobs.get(declaringClass) == null) {
            this.jobs.put(declaringClass, new ConcurrentLinkedQueue<>());
        }
    }
}