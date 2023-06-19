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

package com.clubobsidian.obbylang.manager.database.type.sql;

import com.clubobsidian.obbylang.manager.database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractSQLDatabase extends Database {

    public Map<String, List<Object>> select(String query) {
        return this.select(query, new ArrayList<>());
    }

    public abstract Map<String, List<Object>> select(String query, List<Object> vars);

    public void execute(String query) {
        this.execute(query, new ArrayList<>());
    }

    public abstract boolean execute(String query, List<Object> vars);

    public abstract boolean executeUpdate(String query, List<Object> vars);
}
