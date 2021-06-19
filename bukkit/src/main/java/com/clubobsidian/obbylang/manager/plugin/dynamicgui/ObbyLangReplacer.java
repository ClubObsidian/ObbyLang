package com.clubobsidian.obbylang.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.replacer.Replacer;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;

public class ObbyLangReplacer extends Replacer {

    public ObbyLangReplacer(String toReplace) {
        super(toReplace);
    }

    @Override
    public String replacement(String text, PlayerWrapper<?> playerWrapper) {
        CompiledScript owner = GuiManager.get().getReplacerOwner(this.getToReplace());
        ScriptObjectMirror script = GuiManager.get().getScriptByReplacerName(this.getToReplace());
        Object ret = script.call(owner, playerWrapper, this.getToReplace());
        if(ret == null) {
            return null;
        }

        return (String) ret;
    }
}