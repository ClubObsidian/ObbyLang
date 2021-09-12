package com.clubobsidian.obbylang.velocity.manager;


import com.clubobsidian.obbylang.velocity.event.ObbyLangCustomEvent;
import com.clubobsidian.obbylang.manager.event.CustomEventManager;
import com.clubobsidian.obbylang.velocity.plugin.VelocityObbyLangPlugin;

public class VelocityCustomEventManager extends CustomEventManager {

    @Override
    public void fire(Object[] args) {
        VelocityObbyLangPlugin
                .get()
                .getServer()
                .getEventManager().fire(new ObbyLangCustomEvent(args));
    }
}