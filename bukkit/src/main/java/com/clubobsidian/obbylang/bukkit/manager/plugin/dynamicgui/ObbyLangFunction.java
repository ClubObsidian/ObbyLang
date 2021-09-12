package com.clubobsidian.obbylang.bukkit.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.function.Function;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;

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
        CompiledScript owner = GuiManager.get().getFunctionOwner(this.getName());
        ScriptObjectMirror script = GuiManager.get().getScriptByFunctionName(this.getName());
        Object ret = script.call(owner, playerWrapper, this.getData(), this.getOwner());
        if(ret == null) {
            return true;
        } else if(ret instanceof Boolean) {
            return (boolean) ret;
        }
        return true;
    }
}