package com.clubobsidian.obbylang.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.function.Function;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class ObbyLangFunction extends Function {

    /**
     *
     */
    private static final long serialVersionUID = -3550832874134767689L;

    public ObbyLangFunction(String name) {
        super(name);
    }

    @Override
    public boolean function(PlayerWrapper<?> playerWrapper) {
        Value script = GuiManager.get().getScriptByFunctionName(this.getName());
        Object ret = script.execute(playerWrapper, this.getData(), this.getOwner());
        if(ret == null) {
            return true;
        } else if(ret instanceof Boolean) {
            return (boolean) ret;
        }
        return true;
    }
}