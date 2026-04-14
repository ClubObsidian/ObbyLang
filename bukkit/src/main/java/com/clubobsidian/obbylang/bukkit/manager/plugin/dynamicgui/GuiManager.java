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

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSFunction;
import com.caoccao.qjs4j.core.JSValue;
import com.clubobsidian.dynamicgui.api.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.api.gui.Gui;
import com.clubobsidian.dynamicgui.api.gui.Slot;
import com.clubobsidian.dynamicgui.api.manager.FunctionManager;
import com.clubobsidian.dynamicgui.api.parser.function.FunctionToken;
import com.clubobsidian.dynamicgui.api.parser.function.tree.FunctionTree;
import com.clubobsidian.dynamicgui.api.registry.replacer.DynamicGuiReplacerRegistry;
import com.clubobsidian.dynamicgui.api.replacer.Replacer;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GuiManager implements RegisteredManager {

    private final Map<String, List<String>> functionStrings = new HashMap<>();
    private final Map<String, JSFunction> functionScripts = new HashMap<>();
    private final Map<String, String> functionScriptOwners = new HashMap<>(); //Key(FunctionName) Value(ScriptName)

    private final Map<String, List<String>> replacerStrings = new HashMap<>();
    private final Map<String, JSFunction> replacerScripts = new HashMap<>();
    private final Map<String, String> replacerScriptOwners = new HashMap<>(); //Key(ReplacerName) Value(ScriptName)
    private final ScriptManager scriptManager;

    public GuiManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    private void init(String declaringClass) {
        this.functionStrings.computeIfAbsent(declaringClass, k -> new ArrayList<>());
        this.replacerStrings.computeIfAbsent(declaringClass, k -> new ArrayList<>());
    }

    public void registerFunction(String declaringClass, String functionName, JSFunction script) {
        this.init(declaringClass);
        this.functionStrings.get(declaringClass).add(functionName);
        this.functionScripts.put(functionName, script);
        this.functionScriptOwners.put(functionName, declaringClass);
        FunctionManager.get().registerFunction(new ObbyLangDynamicGuiFunction(functionName, declaringClass));
    }

    public void registerReplacer(String declaringClass, String replacer, JSFunction script) {
        this.init(declaringClass);
        this.replacerStrings.get(declaringClass).add(replacer);
        this.replacerScripts.put(replacer, script);
        this.replacerScriptOwners.put(replacer, declaringClass);
        DynamicGuiReplacerRegistry.get().addReplacer(new Replacer(replacer) {
            @Override
            public String replacement(String text, PlayerWrapper<?> playerWrapper) {
                JSContext owner = script.getContext();
                Object ret = script.call(owner, owner.getCurrentThis(), new JSValue[]{});
                if(ret == null) {
                    return null;
                }
                return (String) ret;
            }
        });
    }

    public JSFunction getScriptByFunctionName(String functionName) {
        return this.functionScripts.get(functionName);
    }

    public JSContext getFunctionOwner(String functionName) {
        String scriptName = this.functionScriptOwners.get(functionName);
        return this.scriptManager.getScript(scriptName);
    }

    public JSFunction getScriptByReplacerName(String replacerName) {
        return this.replacerScripts.get(replacerName);
    }

    public JSContext getReplacerOwner(String replacerName) {
        String scriptName = this.replacerScriptOwners.get(replacerName);
        return this.scriptManager.getScript(scriptName);
    }

    public FunctionToken.Builder createFunctionTokenBuilder() {
        return new FunctionToken.Builder();
    }

    public Gui.Builder createGuiBuilder() {
        return new Gui.Builder();
    }

    public Slot.Builder createSlotBuilder() {
        return new Slot.Builder();
    }

    public FunctionTree createFunctionTree() {
        return new FunctionTree.Builder().build();
    }

    public CompletableFuture<Boolean> openGui(Gui gui, Player player) {
        return com.clubobsidian.dynamicgui.api.manager.gui.GuiManager.get().openGui(player, gui);
    }

    public CompletableFuture<Boolean> openGui(String guiName, Player player) {
        Gui gui = com.clubobsidian.dynamicgui.api.manager.gui.GuiManager.get().getGui(guiName);
        if (gui == null) {
            return CompletableFuture.completedFuture(false);
        }
        return com.clubobsidian.dynamicgui.api.manager.gui.GuiManager.get().openGui(player, gui);
    }

    @Override
    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(String functionName : this.functionStrings.get(declaringClass)) {
            FunctionManager.get().unregisterFunction(functionName);
            this.functionScripts.keySet().remove(functionName);
            this.functionScriptOwners.remove(functionName);
        }

        for(String replacerName : this.replacerStrings.get(declaringClass)) {
            this.replacerScripts.keySet().remove(replacerName);
            this.functionScriptOwners.remove(replacerName);
        }
    }
}