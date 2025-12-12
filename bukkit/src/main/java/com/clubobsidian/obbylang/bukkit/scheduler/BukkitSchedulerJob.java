package com.clubobsidian.obbylang.bukkit.scheduler;

import com.clubobsidian.obbylang.manager.scheduler.SchedulerJob;
import org.bukkit.Bukkit;

public class BukkitSchedulerJob implements SchedulerJob {

    private final int taskId;

    public BukkitSchedulerJob(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean isRunning() {
        return Bukkit.getServer().getScheduler().isCurrentlyRunning(this.taskId);
    }

    @Override
    public void stop() {
        Bukkit.getServer().getScheduler().cancelTask(this.taskId);
    }
}
