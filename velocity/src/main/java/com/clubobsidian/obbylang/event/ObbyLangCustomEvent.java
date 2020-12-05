package com.clubobsidian.obbylang.event;


public class ObbyLangCustomEvent {

    private Object[] args;
    
    public ObbyLangCustomEvent(Object[] args) {
    	this.args = args;
    }
    
    public Object[] getArgs() {
    	return this.args;
    }
}