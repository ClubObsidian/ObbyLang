package com.clubobsidian.obbylang.manager.bungeecord;

import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeCordListenerManager extends ListenerManager<Byte> {

    private final Byte[] priorities;

    public BungeeCordListenerManager() {
        this.priorities = new Byte[]
                {
                        EventPriority.LOWEST,
                        EventPriority.LOW,
                        EventPriority.NORMAL,
                        EventPriority.HIGH,
                        EventPriority.HIGHEST
                };
    }

    @Override
    public String getDefaultPriority() {
        return "NORMAL";
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
    public Byte[] getPriorities() {
        return this.priorities;
    }

    @Override
    public String getPriorityName() {
        return "priority";
    }
}