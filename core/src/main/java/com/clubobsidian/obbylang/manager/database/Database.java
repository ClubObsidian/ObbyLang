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