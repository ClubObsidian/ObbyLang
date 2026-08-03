package com.clubobsidian.obbylang.manager.scheduler;

import com.clubobsidian.crouton.wrapper.JobWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class KotlinSchedulerJob implements SchedulerJob {

    private final JobWrapper wrapper;

    public KotlinSchedulerJob(@NotNull JobWrapper wrapper) {
        this.wrapper = Objects.requireNonNull(wrapper);
    }

    @Override
    public boolean isRunning() {
        return this.wrapper.isRunning();
    }

    @Override
    public void stop() {
        this.wrapper.stop();
    }
}
