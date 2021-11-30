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

public abstract class Database {

    public abstract boolean close();

    public enum Type {
        MYSQL,
        INFLUXDB,
        MONGODB,
        UNKNOWN;

        public static Type fromString(String type) {
            String lowerType = type.toLowerCase();
            if(lowerType.equals("sql") || lowerType.equals("mysql")) {
                return Type.MYSQL;
            } else if(lowerType.equals("influxdb") || lowerType.equals("influx")) {
                return Type.INFLUXDB;
            } else if(lowerType.equals("mongo") || lowerType.equals("mongodb")) {
                return Type.MONGODB;
            }
            return Type.UNKNOWN;
        }
    }
}