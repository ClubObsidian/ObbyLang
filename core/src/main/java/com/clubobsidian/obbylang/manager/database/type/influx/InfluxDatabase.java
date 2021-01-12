package com.clubobsidian.obbylang.manager.database.type.influx;

import com.clubobsidian.obbylang.manager.database.Database;
import com.zaxxer.influx4j.InfluxDB;

public class InfluxDatabase extends Database {

    private InfluxDB db;

    public InfluxDatabase(String ip, int port, String database, String username, String password) {
        this.db = InfluxDB.builder()
                .setConnection(ip, port, InfluxDB.Protocol.HTTP)
                .setUsername(username)
                .setPassword(password)
                .setDatabase(database)
                .build();
    }

    @Override
    public boolean close() {
        return false;
    }
}
