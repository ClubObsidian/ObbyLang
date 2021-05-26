package com.clubobsidian.obbylang.manager.database.type.mongo;

import com.clubobsidian.obbylang.manager.database.Database;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MongoDatabase extends Database {

    private final MongoClient client;
    private final String database;

    public MongoDatabase(String ip, int port, String database, String username, String password, int maxPoolSize) {
        this.client = this.createClient(ip, port, database, username, password);
        this.database = database;
    }

    private MongoClient createClient(String ip, int port, String database, String username, String password) {
        MongoCredential credentials = MongoCredential.createCredential(username, database, password.toCharArray());
        return MongoClients.create(MongoClientSettings.builder().applyToClusterSettings(builder ->
                builder.hosts(Arrays.asList(new ServerAddress(ip, port)))
        ).credential(credentials).build());
    }

    public void createCollection(String name) {
        this.client.getDatabase(this.database).createCollection(name);
    }

    public <T> Document getDocument(String collectionName, String key, T value) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .find(Filters.eq(key, value))
                .first();
    }

    public <T> Collection<Document> getDocuments(String collectionName, String key, T value) {
        Collection<Document> documents = new ArrayList<>();
        this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .find(Filters.eq(key, value)).forEach(document -> {
            documents.add(document);
        });
        return documents;
    }

    @Override
    public boolean close() {
        this.client.close();
        return true;
    }
}
