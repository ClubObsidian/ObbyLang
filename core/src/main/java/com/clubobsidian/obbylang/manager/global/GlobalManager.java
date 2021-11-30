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

package com.clubobsidian.obbylang.manager.global;

import com.google.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalManager {

    private final Map<String, Object> globals = new ConcurrentHashMap<>();

    @Inject
    protected GlobalManager() {

    }

    public void set(String name, Object passed) {
        this.globals.put(name, passed);
    }

    public Object get(String name) {
        return this.globals.get(name);
    }

    public void remove(String name) {
        this.globals.remove(name);
    }

    public Map<String, Object> getGlobals() {
        return this.globals;
    }
}