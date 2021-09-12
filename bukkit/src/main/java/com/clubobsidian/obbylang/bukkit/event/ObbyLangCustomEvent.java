package com.clubobsidian.obbylang.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ObbyLangCustomEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Object[] args;

    public ObbyLangCustomEvent(Object[] args) {
        this.args = args;
    }

    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return ObbyLangCustomEvent.handlers;
    }
}