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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectDescriptor {

    private String name;
    private String description;
    private String version;
    private String main;
    private final List<String> dependencies = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getVersion() {
        return this.version;
    }

    public String getMain() {
        return this.main != null && !this.main.isBlank() ? this.main : "index.js";
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }
}
