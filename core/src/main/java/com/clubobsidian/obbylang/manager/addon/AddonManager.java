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

package com.clubobsidian.obbylang.manager.addon;

import java.util.LinkedHashMap;
import java.util.Map;

public class AddonManager {

    private static AddonManager instance;

    public static AddonManager get() {
        if(instance == null) {
            instance = new AddonManager();
        }
        return instance;
    }

    private final Map<String, Object> addonContext = new LinkedHashMap<>();

    private AddonManager() { }

    public boolean registerAddon(String name, Object object) {
        return this.addonContext.put(name, object) != null;
    }

    public Map<String, Object> getAddons() {
        return new LinkedHashMap<>(this.addonContext);
    }
}