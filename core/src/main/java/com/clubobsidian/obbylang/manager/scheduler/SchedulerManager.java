package com.clubobsidian.obbylang.manager.scheduler;

import com.clubobsidian.crouton.Crouton;
import com.clubobsidian.crouton.wrapper.FutureJobWrapper;
import com.clubobsidian.crouton.wrapper.JobWrapper;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import org.graalvm.polyglot.Value;

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

    private Crouton crouton;
    private Map<String, ConcurrentLinkedQueue<WeakReference<JobWrapper>>> jobs;
    private Queue<Runnable> syncQueue;

    private SchedulerManager() {
        this.crouton = new Crouton();
        this.jobs = new ConcurrentHashMap<>();
        this.syncQueue = new ConcurrentLinkedQueue<>();
        this.startSyncQueueTask();
    }

    private void startSyncQueueTask() {
        FakeServerManager.get().scheduleSyncRepeatingTask(() ->
        {
            Runnable runnable;
            while((runnable = syncQueue.poll()) != null) {
                runnable.run();
            }
        }, 1, 1);
    }

    public synchronized void sync(final String declaringClass, final Value script) {
        this.syncQueue.add(() -> script.executeVoid());
    }

    public JobWrapper syncDelayed(String declaringClass, final Value script, Long delay) {
        this.init(declaringClass);
        JobWrapper wrapper = this.crouton.asyncDelayed(() -> {
            this.syncQueue.add(() -> script.executeVoid());
        }, delay);
        return wrapper;
    }


    public JobWrapper syncRepeating(String declaringClass, final Value script, Long delayStart, Long delayRepeating) {
        this.init(declaringClass);

        JobWrapper wrapper = this.crouton.asyncRepeating(() -> {
            this.syncQueue.add(() -> script.executeVoid());
        }, delayStart, delayRepeating);

        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public JobWrapper async(final String declaringClass, final Value script) {
        this.init(declaringClass);
        JobWrapper wrapper = this.crouton.async(script::executeVoid);
        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public JobWrapper asyncRepeating(final String declaringClass, final Value script, Long delayStart, Long delayRepeating) {
        this.init(declaringClass);

        JobWrapper wrapper = this.crouton.asyncRepeating(() -> {
            script.executeVoid();
        }, delayStart, delayRepeating);

        this.jobs.get(declaringClass).add(new WeakReference<>(wrapper));
        return wrapper;
    }

    public Future<Object> await(String declaringClass, final Value script) {
        return this.asyncWait(declaringClass, script);
    }

    public Future<Object> asyncWait(final String declaringClass, final Value script) {
        this.init(declaringClass);
        FutureJobWrapper wrapper = this.crouton.await(() -> script.execute());
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