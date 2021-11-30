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

import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.dynamicgui.GuiManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.protocollib.ProtocolLibManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.google.inject.Inject;
import org.bukkit.Bukkit;

public class PluginManager {

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

    private boolean resolveDynamicGui() {
        return Bukkit.getServer().getPluginManager().getPlugin("DynamicGui") != null;
    }

    private boolean resolveProtocolLib() {
        return Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null;
    }

    public void addPluginContext() {
        if(this.dynamicGuiEnabled) {
           this.addonManager.registerAddon("guiManager", new GuiManager(this.scriptManager));
        }
        if(this.protocolLibEnabled) {
            this.addonManager.registerAddon("protocolLib", new ProtocolLibManager(this.scriptManager));
        }
    }
}