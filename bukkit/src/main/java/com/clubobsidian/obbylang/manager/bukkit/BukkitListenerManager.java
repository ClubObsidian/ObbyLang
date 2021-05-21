package com.clubobsidian.obbylang.manager.bukkit;

import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BukkitListenerManager extends ListenerManager<EventPriority> {

    @Override
    public String getDefaultPriority() {
        return EventPriority.NORMAL.name();
    }

    @Override
    public Class<?> getHandlerClass() {
        return EventHandler.class;
    }

    @Override
    public Class<?> getListenerClass() {
        return Listener.class;
    }

    @Override
    public Class<?> getEventPriorityClass() {
        return EventPriority.class;
    }

    @Override
    public EventPriority[] getPriorities() {
        return EventPriority.values();
    }

    @Override
    public String getPriorityName() {
        return "priority";
    }
}