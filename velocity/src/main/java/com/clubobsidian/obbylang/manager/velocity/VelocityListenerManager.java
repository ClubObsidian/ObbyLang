package com.clubobsidian.obbylang.manager.velocity;

import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;

public class VelocityListenerManager extends ListenerManager<PostOrder> {

    @Override
    public String getDefaultPriority() {
        return "NORMAL";
    }

    @Override
    public Class<?> getHandlerClass() {
        return Subscribe.class;
    }

    @Override
    public Class<?> getListenerClass() {
        return null;
    }

    @Override
    public Class<?> getEventPriorityClass() {
        return PostOrder.class;
    }

    @Override
    public PostOrder[] getPriorities() {
        return PostOrder.values();
    }

    @Override
    public String getPriorityName() {
        return "order";
    }
}