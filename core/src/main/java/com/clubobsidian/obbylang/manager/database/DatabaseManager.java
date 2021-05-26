package com.clubobsidian.obbylang.manager.database;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.database.type.influx.InfluxDatabase;
import com.clubobsidian.obbylang.manager.database.type.mongo.MongoDatabase;
import com.clubobsidian.obbylang.manager.database.type.sql.MySQLDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseManager implements RegisteredManager {

    private static DatabaseManager instance;

    public static DatabaseManager get() {
        if(instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private final Map<String, List<Database>> databases;

    private DatabaseManager() {
        this.databases = new HashMap<>();
    }

    public Database connect(String declaringClass, String type, String connection, int maxPoolSize) {
        this.init(declaringClass);
        Database database = null;
        Database.Type databaseType = Database.Type.fromString(type);
        if(databaseType == Database.Type.MYSQL) {
            database = new MySQLDatabase(connection, maxPoolSize);
        }
        if(database != null) {
            this.databases.get(declaringClass).add(database);
        }
        return database;
    }

    public Database connect(String declaringClass, String type, String ip, int port, String database, String username, String password, int maxPoolSize) {
        Database.Type databaseType = Database.Type.fromString(type);
        if(databaseType == Database.Type.MYSQL) {
            StringBuilder sb = new StringBuilder();
            sb.append("jdbc:mysql://");
            sb.append(ip);
            sb.append(":");
            sb.append(port);
            sb.append("/");
            sb.append(database);
            sb.append("?user=");
            sb.append(username);
            sb.append("&password=");
            sb.append(password);
            return this.connect(declaringClass, type, sb.toString(), maxPoolSize);
        } else if(databaseType == Database.Type.INFLUXDB) {
            return new InfluxDatabase(ip, port, database, username, password, maxPoolSize);
        } else if(databaseType == Database.Type.MONGODB) {
            return new MongoDatabase(ip, port, database, username, password);
        }
        return null;
    }

    public Database connect(String declaringClass, String type, String connection) {
        this.init(declaringClass);
        Database.Type databaseType = Database.Type.fromString(type);
        if(databaseType == Database.Type.MYSQL) {
            return this.connect(declaringClass, type, connection, 10);
        }
        return null;
    }

    public Database connect(String declaringClass, String type, String ip, int port, String database, String username, String password) {
        int poolSize = -1;
        Database.Type databaseType = Database.Type.fromString(type);
        if(databaseType == Database.Type.MYSQL) {
            poolSize = 10;
        } else if(databaseType == Database.Type.INFLUXDB) {
            poolSize = 10000;
        } else if(databaseType != Database.Type.UNKNOWN) {
            poolSize = 1;
        }
        return poolSize > 0 ? this.connect(declaringClass, type, ip, port, database, username, password, poolSize) : null;
    }


    public void unregister(String declaringClass) {
        this.init(declaringClass);
        for(Database database : this.databases.get(declaringClass)) {
            database.close();
        }
        this.databases.keySet().remove(declaringClass);
    }

    private void init(String declaringClass) {
        if(this.databases.get(declaringClass) == null) {
            this.databases.put(declaringClass, new CopyOnWriteArrayList<>());
        }
    }
}