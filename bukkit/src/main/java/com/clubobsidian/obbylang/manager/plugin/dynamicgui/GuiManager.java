package com.clubobsidian.obbylang.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.builder.EnchantmentBuilder;
import com.clubobsidian.dynamicgui.builder.FunctionBuilder;
import com.clubobsidian.dynamicgui.builder.FunctionTokenBuilder;
import com.clubobsidian.dynamicgui.builder.GuiBuilder;
import com.clubobsidian.dynamicgui.builder.SlotBuilder;
import com.clubobsidian.dynamicgui.entity.bukkit.BukkitPlayerWrapper;
import com.clubobsidian.dynamicgui.gui.Gui;
import com.clubobsidian.dynamicgui.manager.dynamicgui.FunctionManager;
import com.clubobsidian.dynamicgui.parser.function.tree.FunctionTree;
import com.clubobsidian.dynamicgui.registry.replacer.impl.DynamicGuiReplacerRegistry;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.entity.Player;

import javax.script.CompiledScript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiManager implements RegisteredManager {

    private static GuiManager instance;

    private final Map<String, List<String>> functionStrings;
    private final Map<String, ScriptObjectMirror> functionScripts;
    private final Map<String, String> functionScriptOwners; //Key(FunctionName) Value(ScriptName)

    private final Map<String, List<String>> replacerStrings;
    private final Map<String, ScriptObjectMirror> replacerScripts;
    private final Map<String, String> replacerScriptOwners; //Key(ReplacerName) Value(ScriptName)

    public static GuiManager get() {
        if(instance == null) {
            instance = new GuiManager();
        }
        return instance;
    }

    private GuiManager() {
        this.functionStrings = new HashMap<>();
        this.functionScripts = new HashMap<>();
        this.functionScriptOwners = new HashMap<>();

        this.replacerStrings = new HashMap<>();
        this.replacerScripts = new HashMap<>();
        this.replacerScriptOwners = new HashMap<>();
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
        FunctionManager.get().addFunction(new ObbyLangFunction(functionName));
    }

    public void registerReplacer(String declaringClass, String replacer, ScriptObjectMirror script) {
        this.init(declaringClass);
        this.replacerStrings.get(declaringClass).add(replacer);
        this.replacerScripts.put(replacer, script);
        this.replacerScriptOwners.put(replacer, declaringClass);
        DynamicGuiReplacerRegistry.get().addReplacer(new ObbyLangReplacer(replacer));
    }

    public ScriptObjectMirror getScriptByFunctionName(String functionName) {
        return this.functionScripts.get(functionName);
    }

    public CompiledScript getFunctionOwner(String functionName) {
        String scriptName = this.functionScriptOwners.get(functionName);
        return ScriptManager.get().getScript(scriptName);
    }

    public ScriptObjectMirror getScriptByReplacerName(String replacerName) {
        return this.replacerScripts.get(replacerName);
    }

    public CompiledScript getReplacerOwner(String replacerName) {
        String scriptName = this.replacerScriptOwners.get(replacerName);
        return ScriptManager.get().getScript(scriptName);
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
        com.clubobsidian.dynamicgui.manager.dynamicgui.GuiManager.get().openGui(new BukkitPlayerWrapper<Player>(player), gui);
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