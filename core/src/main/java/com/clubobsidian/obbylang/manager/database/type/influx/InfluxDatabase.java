package com.clubobsidian.obbylang.manager.database.type.influx;

import com.clubobsidian.obbylang.manager.database.Database;
import com.zaxxer.influx4j.InfluxDB;
import com.zaxxer.influx4j.PointFactory;

public class InfluxDatabase extends Database {

    private InfluxDB db;
    private PointFactory factory;

    public InfluxDatabase(String ip, int port, String database, String username, String password, int maxPoolSize) {
        this.db = this.createDatabase(ip, port, database, username, password);
        this.factory = this.createFactory(maxPoolSize);
    }

    @Override
    public boolean close() {
        return false;
    }

    private InfluxDB createDatabase(String ip, int port, String database, String username, String password) {
        return InfluxDB.builder()
                .setConnection(ip, port, InfluxDB.Protocol.HTTP)
                .setUsername(username)
                .setPassword(password)
                .setDatabase(database)
                .build();
    }

    private PointFactory createFactory(int maxPoolSize) {
        return PointFactory.builder()
                .initialSize(maxPoolSize / 4)
                .maximumSize(maxPoolSize)
                .build();
    }
}
