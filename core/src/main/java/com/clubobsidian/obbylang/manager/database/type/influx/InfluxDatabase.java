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

package com.clubobsidian.obbylang.manager.database.type.influx;

import com.clubobsidian.obbylang.manager.database.Database;
import com.zaxxer.influx4j.InfluxDB;
import com.zaxxer.influx4j.Point;
import com.zaxxer.influx4j.PointFactory;

public class InfluxDatabase extends Database {

    private final InfluxDB db;
    private final PointFactory factory;

    public InfluxDatabase(String ip, int port, String database, String username, String password, int maxPoolSize) {
        this.db = this.createDatabase(ip, port, database, username, password);
        this.factory = this.createFactory(maxPoolSize);
    }

    @Override
    public boolean close() {
        this.db.close();
        return true;
    }

    private InfluxDB createDatabase(String ip, int port, String database, String username, String password) {
        return InfluxDB.builder()
                .setConnection(ip, port, InfluxDB.Protocol.HTTP)
                .setUsername(username)
                .setPassword(password)
                .setDatabase(database)
                .build();
    }

    public Point create(String tag) {
        return this.factory.createPoint(tag);
    }

    public void write(Point point) {
        this.db.write(point);
    }

    private PointFactory createFactory(int maxPoolSize) {
        return PointFactory.builder()
                .initialSize(maxPoolSize / 4)
                .maximumSize(maxPoolSize)
                .build();
    }
}