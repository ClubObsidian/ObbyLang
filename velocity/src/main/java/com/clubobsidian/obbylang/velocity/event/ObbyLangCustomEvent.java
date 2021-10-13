package com.clubobsidian.obbylang.velocity.event;


public class ObbyLangCustomEvent {

    private final Object[] args;

    public ObbyLangCustomEvent(Object[] args) {
        this.args = args;
    }

    public Object[] getArgs() {
        return this.args;
    }
}