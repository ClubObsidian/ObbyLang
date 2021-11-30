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

package com.clubobsidian.obbylang.manager.database;

import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.database.type.influx.InfluxDatabase;
import com.clubobsidian.obbylang.manager.database.type.mongo.MongoDatabase;
import com.clubobsidian.obbylang.manager.database.type.sql.MySQLDatabase;
import com.google.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseManager implements RegisteredManager {

    private final Map<String, List<Database>> databases = new ConcurrentHashMap<>();

    @Inject
    private DatabaseManager() {

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
        }
        return null;
    }

    public Database connect(String declaringClass, String type, String connection) {
        this.init(declaringClass);
        Database.Type databaseType = Database.Type.fromString(type);
        if(databaseType == Database.Type.MYSQL) {
            return this.connect(declaringClass, type, connection, 10);
        } else if(databaseType == Database.Type.MONGODB) {
            return new MongoDatabase(connection);
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