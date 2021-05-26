package com.clubobsidian.obbylang.manager.database.type.mongo;

import com.clubobsidian.obbylang.manager.database.Database;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MongoDatabase extends Database {

    private final MongoClient client;
    private final String database;

    public MongoDatabase(String ip, int port, String database, String username, String password) {
        this.client = this.createClient(ip, port, database, username, password);
        this.database = database;
    }

    private MongoClient createClient(String ip, int port, String database, String username, String password) {
        MongoCredential credentials = MongoCredential.createCredential(username, database, password.toCharArray());
        return MongoClients.create(MongoClientSettings.builder().applyToClusterSettings(builder ->
                builder.hosts(Collections.singletonList(new ServerAddress(ip, port)))
        ).credential(credentials).build());
    }

    public void createCollection(String name) {
        this.client.getDatabase(this.database).createCollection(name);
    }

    public void deleteCollection(String name) {
        this.client.getDatabase(this.database).getCollection(name).drop();
    }

    public boolean collectionExists(String name) {
        return this.client.getDatabase(this.database)
                .listCollectionNames()
                .into(new ArrayList<>())
                .contains(name);
    }

    public BasicDBObject createObject() {
        return new BasicDBObject();
    }

    public <T> BasicDBObject createObject(String key, T value) {
        return new BasicDBObject(key, value);
    }

    public BasicDBObject createObject(String json) {
        return BasicDBObject.parse(json);
    }

    public Document createDocument() {
        return new Document();
    }

    public <T> Document createDocument(String key, T value) {
        return new Document(key, value);
    }

    public Document createDocument(String json) {
        return Document.parse(json);
    }

    public <T> Document getDocument(String collectionName, String key, T value) {
        return this.getDocument(collectionName, new BasicDBObject(key, value));
    }

    public Document getDocument(String collectionName, Bson obj) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .find(Filters.eq(obj))
                .first();
    }

    public <T> Collection<Document> getManyDocuments(String collectionName, String key, T value) {
        return this.getManyDocuments(collectionName, this.createObject(key, value));
    }

    public Collection<Document> getManyDocuments(String collectionName, BasicDBObject obj) {
       return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .find(Filters.eq(obj))
                .into(new ArrayList<>());
    }

    public <T> boolean insertDocument(String collectionName, String key, T value) {
        return this.insertDocument(collectionName, this.createDocument(key, value));
    }

    public boolean insertDocument(String collectionName, Document document) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .insertOne(document).wasAcknowledged();
    }

    public boolean insertManyDocuments(String collectionName, Document... documents) {
        return this.insertManyDocuments(collectionName, Arrays.asList(documents));
    }

    public boolean insertManyDocuments(String collectionName, Collection<Document> documents) {
        return this.insertManyDocuments(collectionName, new ArrayList<>(documents));
    }

    public boolean insertManyDocuments(String collectionName, List<Document> documents) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .insertMany(documents)
                .wasAcknowledged();
    }

    public Document updateDocument(String collectionName, Bson filter, Bson update) {
        return this.updateDocument(collectionName, filter, new Bson[]{update});
    }

    public Document updateDocument(String collectionName, Bson filter, Bson... update) {
        return this.updateDocument(collectionName, filter, Arrays.asList(update));
    }

    public Document updateDocument(String collectionName, Bson filter, Collection<Bson> update) {
        return this.updateDocument(collectionName, filter, new ArrayList<>(update));
    }

    public Document updateDocument(String collectionName, Bson filter, List<Bson> update) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .findOneAndUpdate(filter, update, options);
    }

    public long updateManyDocuments(String collectionName, Bson filter, Bson update) {
        return this.updateManyDocuments(collectionName, filter, new Bson[]{update});
    }

    public long updateManyDocuments(String collectionName, Bson filter, Bson... update) {
        return this.updateManyDocuments(collectionName, filter, Arrays.asList(update));
    }

    public long updateManyDocuments(String collectionName, Bson filter, Collection<Bson> update) {
        return this.updateManyDocuments(collectionName, filter, new ArrayList<>(update));
    }

    public long updateManyDocuments(String collectionName, Bson filter, List<Bson> update) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .updateMany(filter, update)
                .getModifiedCount();
    }

    public boolean deleteDocument(String collectionName, Bson filter) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .deleteOne(filter)
                .wasAcknowledged();
    }

    public long deleteManyDocuments(String collectionName, Bson filter) {
        return this.client.getDatabase(this.database)
                .getCollection(collectionName)
                .deleteMany(filter)
                .getDeletedCount();
    }

    @Override
    public boolean close() {
        this.client.close();
        return true;
    }
}
