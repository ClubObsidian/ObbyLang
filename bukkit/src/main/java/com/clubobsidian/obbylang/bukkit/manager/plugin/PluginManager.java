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

package com.clubobsidian.obbylang.bukkit.manager.plugin;

import com.clubobsidian.obbylang.bukkit.manager.plugin.dynamicgui.GuiManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.protocollib.ProtocolLibManager;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import org.bukkit.Bukkit;

import javax.inject.Inject;

public class PluginManager {

    public static final String DYNAMIC_GUI = "guiManager";
    public static final String PROTOCOL_LIB = "protocolLib";

    private final boolean dynamicGuiEnabled = this.resolveDynamicGui();
    private final boolean protocolLibEnabled = this.resolveProtocolLib();
    private final AddonManager addonManager;
    private final ScriptManager scriptManager;

    @Inject
    private PluginManager(AddonManager addonManager, ScriptManager scriptManager) {
        this.addonManager = addonManager;
        this.scriptManager = scriptManager;
        this.addPluginContext();
    }

    public boolean isDynamicGuiEnabled() {
        return this.dynamicGuiEnabled;
    }

    public boolean isProtocolLibEnabled() {
        return this.protocolLibEnabled;
    }

    public AddonManager getAddonManager() {
        return this.addonManager;
    }

    private boolean resolveDynamicGui() {
        boolean classExists = this.classExists("com.clubobsidian.dynamicgui.core.function.Function");
        return Bukkit.getServer().getPluginManager().getPlugin("DynamicGui") != null && classExists;
    }

    private boolean resolveProtocolLib() {
        return Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null;
    }

    public void addPluginContext() {
        if(this.dynamicGuiEnabled) {
            this.addonManager.registerAddon(DYNAMIC_GUI, new GuiManager(this.scriptManager));
        }
        if(this.protocolLibEnabled) {
            this.addonManager.registerAddon(PROTOCOL_LIB, new ProtocolLibManager(this.scriptManager));
        }
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch(ClassNotFoundException ex) {
            return false;
        }
    }
}