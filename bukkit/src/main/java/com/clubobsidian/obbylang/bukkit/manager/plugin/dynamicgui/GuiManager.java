/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.bukkit.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.core.builder.EnchantmentBuilder;
import com.clubobsidian.dynamicgui.core.builder.FunctionBuilder;
import com.clubobsidian.dynamicgui.core.builder.FunctionTokenBuilder;
import com.clubobsidian.dynamicgui.core.builder.GuiBuilder;
import com.clubobsidian.dynamicgui.core.builder.SlotBuilder;
import com.clubobsidian.dynamicgui.core.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.core.function.Function;
import com.clubobsidian.dynamicgui.core.gui.Gui;
import com.clubobsidian.dynamicgui.core.manager.dynamicgui.FunctionManager;
import com.clubobsidian.dynamicgui.core.registry.replacer.impl.DynamicGuiReplacerRegistry;
import com.clubobsidian.dynamicgui.core.replacer.Replacer;
import com.clubobsidian.dynamicgui.parser.function.tree.FunctionTree;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import org.bukkit.entity.Player;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiManager implements RegisteredManager {

    private final Map<String, List<String>> functionStrings = new HashMap<>();
    private final Map<String, ScriptObjectMirror> functionScripts = new HashMap<>();
    private final Map<String, String> functionScriptOwners = new HashMap<>(); //Key(FunctionName) Value(ScriptName)

    private final Map<String, List<String>> replacerStrings = new HashMap<>();
    private final Map<String, ScriptObjectMirror> replacerScripts = new HashMap<>();
    private final Map<String, String> replacerScriptOwners = new HashMap<>(); //Key(ReplacerName) Value(ScriptName)
    private final ScriptManager scriptManager;

    public GuiManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    private void init(String declaringClass) {
        if(this.functionStrings.get(declaringClass) == null) {
            this.functionStrings.put(declaringClass, new ArrayList<>());
        }
        if(this.replacerScripts.get(declaringClass) == null) {
            this.replacerStrings.put(declaringClass, new ArrayList<>());
        }
    }

    public void registerFunction(String declaringClass, String functionName, ScriptObjectMirror script) {
        this.init(declaringClass);
        this.functionStrings.get(declaringClass).add(functionName);
        this.functionScripts.put(functionName, script);
        this.functionScriptOwners.put(functionName, declaringClass);
        FunctionManager.get().addFunction(new ObbyLangDynamicGuiFunction(functionName));
    }

    public void registerReplacer(String declaringClass, String replacer, ScriptObjectMirror script) {
        this.init(declaringClass);
        this.replacerStrings.get(declaringClass).add(replacer);
        this.replacerScripts.put(replacer, script);
        this.replacerScriptOwners.put(replacer, declaringClass);
        DynamicGuiReplacerRegistry.get().addReplacer(new Replacer(replacer) {
            @Override
            public String replacement(String text, PlayerWrapper<?> playerWrapper) {
                CompiledScript owner = getReplacerOwner(this.getToReplace());
                ScriptObjectMirror script = getScriptByReplacerName(this.getToReplace());
                Object ret = script.call(owner, playerWrapper, this.getToReplace());
                if(ret == null) {
                    return null;
                }
                return (String) ret;
            }
        });
    }

    public ScriptObjectMirror getScriptByFunctionName(String functionName) {
        return this.functionScripts.get(functionName);
    }

    public CompiledScript getFunctionOwner(String functionName) {
        String scriptName = this.functionScriptOwners.get(functionName);
        return this.scriptManager.getScript(scriptName);
    }

    public ScriptObjectMirror getScriptByReplacerName(String replacerName) {
        return this.replacerScripts.get(replacerName);
    }

    public CompiledScript getReplacerOwner(String replacerName) {
        String scriptName = this.replacerScriptOwners.get(replacerName);
        return this.scriptManager.getScript(scriptName);
    }

    public EnchantmentBuilder createEnchantmentBuilder() {
        return new EnchantmentBuilder();
    }

    public FunctionBuilder createFunctionBuilder() {
        return new FunctionBuilder();
    }

    public FunctionTokenBuilder createFunctionTokenBuilder() {
        return new FunctionTokenBuilder();
    }

    public GuiBuilder createGuiBuilder() {
        return new GuiBuilder();
    }

    public SlotBuilder createSlotBuilder() {
        return new SlotBuilder();
    }

    public FunctionTree createFunctionTree() {
        return new FunctionTree();
    }

    public void openGui(Gui gui, Player player) {
        com.clubobsidian.dynamicgui.core.manager.dynamicgui.GuiManager.get().openGui(player, gui);
    }

    @Override
    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(String functionName : this.functionStrings.get(declaringClass)) {
            FunctionManager.get().removeFunctionByName(functionName);
            this.functionScripts.keySet().remove(functionName);
            this.functionScriptOwners.remove(functionName);
        }

        for(String replacerName : this.replacerStrings.get(declaringClass)) {
            this.replacerScripts.keySet().remove(replacerName);
            this.functionScriptOwners.remove(replacerName);
        }
    }
}