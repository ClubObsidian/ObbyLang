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

package com.clubobsidian.obbylang.bukkit.manager.plugin.dynamicgui;

import com.clubobsidian.dynamicgui.entity.PlayerWrapper;
import com.clubobsidian.dynamicgui.replacer.Replacer;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;

public class ObbyLangReplacer extends Replacer {

    public ObbyLangReplacer(String toReplace) {
        super(toReplace);
    }

    @Override
    public String replacement(String text, PlayerWrapper<?> playerWrapper) {
        CompiledScript owner = GuiManager.get().getReplacerOwner(this.getToReplace());
        ScriptObjectMirror script = GuiManager.get().getScriptByReplacerName(this.getToReplace());
        Object ret = script.call(owner, playerWrapper, this.getToReplace());
        if(ret == null) {
            return null;
        }

        return (String) ret;
    }
}