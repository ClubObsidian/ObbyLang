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

import com.clubobsidian.dynamicgui.api.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.api.function.Function;
import com.clubobsidian.obbylang.bukkit.manager.plugin.PluginManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import com.clubobsidian.obbylang.manager.addon.AddonManager;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;

public class ObbyLangDynamicGuiFunction extends Function {

    public ObbyLangDynamicGuiFunction(String name) {
        super(name);
    }

    @Override
    public boolean function(PlayerWrapper<?> playerWrapper) {
        AddonManager addon = BukkitObbyLangPlugin.get().getAddonManager();
        GuiManager manager = addon.getAddon(PluginManager.DYNAMIC_GUI);
        CompiledScript owner = manager.getFunctionOwner(this.getName());
        ScriptObjectMirror script = manager.getScriptByFunctionName(this.getName());
        Object ret = script.call(owner, playerWrapper, this.getData(), this.getOwner());
        if(ret == null) {
            return true;
        } else if(ret instanceof Boolean) {
            return (boolean) ret;
        }
        return true;
    }
}
