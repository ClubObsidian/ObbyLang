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

package com.clubobsidian.obbylang.manager.script;

import com.caoccao.qjs4j.core.JSContext;
import com.caoccao.qjs4j.core.JSFunction;
import com.caoccao.qjs4j.core.JSValue;
import com.clubobsidian.obbylang.manager.RegisteredManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisableManager implements RegisteredManager {

    private final Map<String, List<JSValue>> disableFunctions = new ConcurrentHashMap<>();

    private final ScriptManager scriptManager;

    @Inject
    private DisableManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    public void register(String declaringClass, JSValue script) {
        this.init(declaringClass);
        this.disableFunctions.get(declaringClass).add(script);
    }

    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(JSValue script : this.disableFunctions.get(declaringClass)) {
            JSFunction function = script.asFunction().get();
            JSContext owner = function.getContext();
            function.call(owner, owner.getCurrentThis(), new JSValue[]{});
        }
        this.disableFunctions.keySet().remove(declaringClass);
    }

    private void init(String declaringClass) {
        this.disableFunctions.computeIfAbsent(declaringClass, k -> new ArrayList<>());
    }
}