/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.bukkit.manager.plugin;

import com.clubobsidian.obbylang.manager.addon.AddonManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.dynamicgui.GuiManager;
import com.clubobsidian.obbylang.bukkit.manager.plugin.protocollib.ProtocolLibManager;
import org.bukkit.Bukkit;

public class PluginManager {

    private static PluginManager instance;

    public static PluginManager get() {
        if(instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    private final boolean dynamicGuiEnabled;
    private final boolean protocolLibEnabled;

    private PluginManager() {
        this.dynamicGuiEnabled = this.resolveDynamicGui();
        this.protocolLibEnabled = this.resolveProtocolLib();
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
            AddonManager.get().registerAddon("guiManager", GuiManager.get());
        }
        if(this.protocolLibEnabled) {
            AddonManager.get().registerAddon("protocolLib", ProtocolLibManager.get());
        }
    }
}