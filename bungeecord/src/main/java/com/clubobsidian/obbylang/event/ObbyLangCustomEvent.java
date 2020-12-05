package com.clubobsidian.obbylang.event;

import net.md_5.bungee.api.plugin.Event;

public class ObbyLangCustomEvent extends Event {

    private Object[] args;
    public ObbyLangCustomEvent(Object[] args)
    {
    	this.args = args;
    }
    
    public Object[] getArgs()
    {
    	return this.args;
    }
}