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

package com.clubobsidian.obbylang.manager.script;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.google.inject.Inject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.CompiledScript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisableManager implements RegisteredManager {

    private final Map<String, List<ScriptObjectMirror>> disableFunctions = new ConcurrentHashMap<>();

    private final ScriptManager scriptManager;

    @Inject
    private DisableManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    public void register(String declaringClass, ScriptObjectMirror script) {
        this.init(declaringClass);
        this.disableFunctions.get(declaringClass).add(script);
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        CompiledScript owner = this.scriptManager.getScript(declaringClass);
        for(ScriptObjectMirror script : this.disableFunctions.get(declaringClass)) {
            script.call(owner);
        }
        this.disableFunctions.keySet().remove(declaringClass);
    }

    private void init(String declaringClass) {
        if(this.disableFunctions.get(declaringClass) == null) {
            this.disableFunctions.put(declaringClass, new ArrayList<>());
        }
    }
}