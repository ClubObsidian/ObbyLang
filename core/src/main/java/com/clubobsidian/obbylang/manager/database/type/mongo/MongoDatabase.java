package com.clubobsidian.obbylang.manager.database.type.mongo;

import com.clubobsidian.obbylang.manager.database.Database;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.Arrays;

public class MongoDatabase extends Database {

    private MongoClient client;

    public MongoDatabase(String ip, int port, String database, String username, String password, int maxPoolSize) {
        this.client = this.createClient(ip, port, database, username, password);
    }

    private MongoClient createClient(String ip, int port, String database, String username, String password) {
        MongoCredential credentials = MongoCredential.createCredential(username, database, password.toCharArray()));
        return MongoClients.create(MongoClientSettings.builder().applyToClusterSettings(builder -> {
            builder.hosts(Arrays.asList(new ServerAddress(ip, port)))
        }).credential(credentials).build());
    }

    public void createDatabase(String name) {
        //this.client.getDatabase("test").
    }

    @Override
    public boolean close() {
        this.client.close();
        return true;
    }
}
