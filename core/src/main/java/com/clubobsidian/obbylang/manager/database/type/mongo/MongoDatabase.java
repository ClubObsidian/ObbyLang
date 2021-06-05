package com.clubobsidian.obbylang.manager.database.type.mongo;

import com.clubobsidian.obbylang.manager.database.Database;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MongoDatabase extends Database {

    private final MongoClient client;

    public MongoDatabase(String connection) {
        this.client = MongoClients.create(connection);
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

    public IndexOptions createIndexOptions() {
        return new IndexOptions();
    }

    public void runCommand(String database, String json) {
        this.client.getDatabase(database).runCommand(BasicDBObject.parse(json));
    }

    public void createCollection(String database, String name) {
        this.client.getDatabase(database).createCollection(name);
    }

    public void deleteCollection(String database, String name) {
        this.client.getDatabase(database).getCollection(name).drop();
    }

    public void createCollectionIndex(String database, String name, Bson obj) {
        this.client.getDatabase(database).getCollection(name).createIndex(obj);
    }

    public boolean collectionExists(String database, String name) {
        return this.client.getDatabase(database)
                .listCollectionNames()
                .into(new ArrayList<>())
                .contains(name);
    }

    public <T> Document getDocument(String database, String collectionName, String key, T value) {
        return this.getDocument(database, collectionName, new BasicDBObject(key, value));
    }

    public Document getDocument(String database, String collectionName, Bson obj) {
        return this.client.getDatabase(database)
                .getCollection(collectionName)
                .find(Filters.eq(obj))
                .first();
    }

    public <T> Collection<Document> getManyDocuments(String database, String collectionName, String key, T value) {
        return this.getManyDocuments(database, collectionName, this.createObject(key, value));
    }

    public Collection<Document> getManyDocuments(String database, String collectionName, BasicDBObject obj) {
       return this.client.getDatabase(database)
                .getCollection(collectionName)
                .find(Filters.eq(obj))
                .into(new ArrayList<>());
    }

    public <T> boolean insertDocument(String database, String collectionName, String key, T value) {
        return this.insertDocument(database, collectionName, this.createDocument(key, value));
    }

    public boolean insertDocument(String database, String collectionName, Document document) {
        return this.client.getDatabase(database)
                .getCollection(collectionName)
                .insertOne(document).wasAcknowledged();
    }

    public boolean insertManyDocuments(String database, String collectionName, Document... documents) {
        return this.insertManyDocuments(database, collectionName, Arrays.asList(documents));
    }

    public boolean insertManyDocuments(String database, String collectionName, Collection<Document> documents) {
        return this.insertManyDocuments(database, collectionName, new ArrayList<>(documents));
    }

    public boolean insertManyDocuments(String database, String collectionName, List<Document> documents) {
        return this.client.getDatabase(database)
                .getCollection(collectionName)
                .insertMany(documents)
                .wasAcknowledged();
    }

    public Document updateDocument(String database, String collectionName, Bson filter, Bson update) {
        return this.updateDocument(database, collectionName, filter, new Bson[]{update});
    }

    public Document updateDocument(String database, String collectionName, Bson filter, Bson... update) {
        return this.updateDocument(database, collectionName, filter, Arrays.asList(update));
    }

    public Document updateDocument(String database, String collectionName, Bson filter, Collection<Bson> update) {
        return this.updateDocument(database, collectionName, filter, new ArrayList<>(update));
    }

    public Document updateDocument(String database, String collectionName, Bson filter, List<Bson> update) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);
        return this.client.getDatabase(database)
                .getCollection(collectionName)
                .findOneAndUpdate(filter, update, options);
    }

    public long updateManyDocuments(String database, String collectionName, Bson filter, Bson update) {
        return this.updateManyDocuments(database, collectionName, filter, new Bson[]{update});
    }

    public long updateManyDocuments(String database, String collectionName, Bson filter, Bson... update) {
        return this.updateManyDocuments(database, collectionName, filter, Arrays.asList(update));
    }

    public long updateManyDocuments(String database, String collectionName, Bson filter, Collection<Bson> update) {
        return this.updateManyDocuments(database, collectionName, filter, new ArrayList<>(update));
    }

    public long updateManyDocuments(String database, String collectionName, Bson filter, List<Bson> update) {
        return this.client.getDatabase(database)
                .getCollection(collectionName)
                .updateMany(filter, update)
                .getModifiedCount();
    }

    public boolean deleteDocument(String database, String collectionName, Bson filter) {
        return this.client.getDatabase(database)
                .getCollection(collectionName)
                .deleteOne(filter)
                .wasAcknowledged();
    }

    public long deleteManyDocuments(String database, String collectionName, Bson filter) {
        return this.client.getDatabase(database)
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
