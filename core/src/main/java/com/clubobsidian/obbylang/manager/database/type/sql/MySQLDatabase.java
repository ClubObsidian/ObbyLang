package com.clubobsidian.obbylang.manager.database.type.sql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLDatabase extends AbstractSQLDatabase {

    private final HikariDataSource source;

    public MySQLDatabase(String connection, int maxPoolSize) {
        this.source = new HikariDataSource();
        this.source.setMaximumPoolSize(maxPoolSize);
        this.source.setJdbcUrl(connection);
    }

    @Override
    public Map<String, List<Object>> select(String query, List<Object> vars) {
        try(Connection con = this.source.getConnection()) {
            PreparedStatement statement = con.prepareStatement(query);
            for(int i = 0; i < vars.size(); i++) {
                statement.setObject(i + 1, vars.get(i));
            }
            Map<String, List<Object>> selected = new HashMap<String, List<Object>>();
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                for(int i = 0; i < statement.getMetaData().getColumnCount(); i++) {
                    String colName = statement.getMetaData().getColumnName(i + 1);
                    if(!selected.containsKey(colName)) {
                        selected.put(colName, new ArrayList<>());
                    }
                    selected.get(colName).add(resultSet.getObject(colName));
                }
            }
            return selected;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean execute(String query, List<Object> vars) {
        try(Connection con = this.source.getConnection()) {
            PreparedStatement statement = con.prepareStatement(query);
            for(int i = 0; i < vars.size(); i++) {
                statement.setObject(i + 1, vars.get(i));
            }
            statement.executeUpdate();
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean close() {
        this.source.close();
        return this.source.isClosed();
    }
}