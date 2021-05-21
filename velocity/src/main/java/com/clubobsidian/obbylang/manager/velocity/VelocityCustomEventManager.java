package com.clubobsidian.obbylang.manager.velocity;


import com.clubobsidian.obbylang.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.plugin.VelocityObbyLangPlugin;

public class VelocityCustomEventManager extends CustomEventManager {

    @Override
    public void fire(Object[] args) {
        VelocityObbyLangPlugin
                .get()
                .getServer()
                .getEventManager().fire(new ObbyLangCustomEvent(args));
    }
}