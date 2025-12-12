package com.clubobsidian.obbylang.bukkit.scheduler;

import com.clubobsidian.obbylang.manager.scheduler.SchedulerJob;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

public class BukkitSchedulerJob implements SchedulerJob {

    private final int taskId;

    public BukkitSchedulerJob(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean isRunning() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        return scheduler.isCurrentlyRunning(this.taskId) || scheduler.isQueued(this.taskId);
    }

    @Override
    public void stop() {
        Bukkit.getServer().getScheduler().cancelTask(this.taskId);
    }
}
