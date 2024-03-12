package com.example.npe;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NpeNativeMongoClientTests {
    private static final Document Index = new Document("ok", 1);

    @Autowired
    MongoClient mongoClient;

    @BeforeEach
    void setup() {
        final var database = mongoClient.getDatabase("test");
        database.createCollection("test");
        final var collection = database.getCollection("test");
        collection.createIndex(Index);
        collection.insertOne(new Document("ok", true));
    }

    @AfterEach
    void cleanup() {
        mongoClient.getDatabase("test").getCollection("test").drop();
    }

    @Test
    void queryWithHintShouldWork() {
        final var collection = mongoClient.getDatabase("test").getCollection("test");
        final var result = collection.find(new Document("ok", true)).hint(Index).first();
        Assertions.assertTrue(result.getBoolean("ok"));
    }

    @Test
    void updateWithHintShouldFail() {
        final var collection = mongoClient.getDatabase("test").getCollection("test");
        final var query = new Document("ok", true);
        final var update = new Document("$set", new Document("update_ok", true));
        final var options = new UpdateOptions().hint(Index);
        // fail with npe
        Assertions.assertThrows(NullPointerException.class, () -> collection.updateMany(query, update, options));
        Assertions.assertThrows(NullPointerException.class, () -> collection.updateOne(query, update, options));
    }


    // Workaround waiting for the fix
    @Test
    void updateWithHintWorkaroundUsingBsonDocumentShouldWork() {
        final var collection = mongoClient.getDatabase("test").getCollection("test");
        final var query = new Document("ok", true);
        final var update = new Document("$set", new Document("update_ok", true));
        // use BsonDocument to specify index
        final var options = new UpdateOptions().hint(new BsonDocument("ok", new BsonInt32(1)));
        collection.updateMany(query, update, options);
        collection.updateOne(query, update, options);
    }
}
