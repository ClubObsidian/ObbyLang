package com.clubobsidian.obbylang.discord.manager.listener;

import com.clubobsidian.obbylang.manager.listener.ListenerManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class DiscordListenerManager extends ListenerManager<String> {

    @Override
    public String getDefaultPriority() {
        return "NORMAL";
    }

    @Override
    public Class<?> getHandlerClass() {
        return SubscribeEvent.class;
    }

    @Override
    public Class<?> getListenerClass() {
        return null;
    }

    @Override
    public Class<?> getEventPriorityClass() {
        return null;
    }

    @Override
    public String[] getPriorities() {
        return new String[]{"NORMAL"};
    }

    @Override
    public String getPriorityName() {
        return null;
    }
}