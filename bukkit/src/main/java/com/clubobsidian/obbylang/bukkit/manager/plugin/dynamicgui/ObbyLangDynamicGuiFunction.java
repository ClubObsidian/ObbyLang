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
import com.caoccao.qjs4j.core.JSString;
import com.caoccao.qjs4j.core.JSValue;
import com.clubobsidian.dynamicgui.api.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.api.function.Function;
import com.clubobsidian.obbylang.bukkit.manager.plugin.PluginManager;
import com.clubobsidian.obbylang.bukkit.plugin.BukkitObbyLangPlugin;
import com.clubobsidian.obbylang.compat.NashornJavaCompat;
import com.clubobsidian.obbylang.manager.addon.AddonManager;

public class ObbyLangDynamicGuiFunction extends Function {

    private final String declaringClass;

    public ObbyLangDynamicGuiFunction(String name, String declaringClass) {
        super(name);
        this.declaringClass = declaringClass;
    }

    @Override
    public boolean function(PlayerWrapper<?> playerWrapper) {
        AddonManager addon = BukkitObbyLangPlugin.get().getAddonManager();
        GuiManager manager = addon.getAddon(PluginManager.DYNAMIC_GUI);
        JSFunction script = manager.getScriptByFunctionName(this.getName());
        JSContext context = script.getContext();
        JSValue ret = script.call(context, context.getCurrentThis(), new JSValue[]{
                NashornJavaCompat.wrapJavaObject(declaringClass, playerWrapper),
                new JSString(this.getData()),
                NashornJavaCompat.wrapJavaObject(declaringClass, this.getOwner())
        });
        if(ret == null) {
            return true;
        } else if(ret.isBoolean()) {
            return ret.asBoolean().get().value();
        }
        return true;
    }
}
