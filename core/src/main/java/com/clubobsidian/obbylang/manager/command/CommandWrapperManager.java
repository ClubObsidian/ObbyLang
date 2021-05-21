package com.clubobsidian.obbylang.manager.command;

import com.clubobsidian.obbylang.ObbyLang;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public abstract class CommandWrapperManager<T> {

    private static CommandWrapperManager<?> instance;

    public static CommandWrapperManager<?> get() {
        if(instance == null) {
            instance = ObbyLang.get().getPlugin().getInjector().getInstance(CommandWrapperManager.class);
        }
        return instance;
    }

    public abstract CommandWrapper<T> createCommandWrapper(String declaringClass, String command, ScriptObjectMirror script);

}