package com.clubobsidian.obbylang.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.replacer.Replacer;
import org.graalvm.polyglot.Value;

public class ObbyLangReplacer extends Replacer {

    public ObbyLangReplacer(String toReplace) {
        super(toReplace);
    }

    @Override
    public String replacement(String text, PlayerWrapper<?> playerWrapper) {
        Value script = GuiManager.get().getScriptByReplacerName(this.getToReplace());
        Object ret = script.execute(playerWrapper, this.getToReplace());
        if(ret == null) {
            return null;
        }

        return (String) ret;
    }
}