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

}
