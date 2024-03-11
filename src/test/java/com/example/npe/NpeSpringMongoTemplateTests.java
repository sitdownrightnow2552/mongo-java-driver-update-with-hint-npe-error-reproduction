package com.example.npe;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@SpringBootTest
class NpeSpringMongoTemplateTests {
    private static final Document Index = new Document("ok", 1);

    @Autowired
    MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        mongoTemplate.createCollection("test");
        final CompoundIndexDefinition indexDefinition = new CompoundIndexDefinition(Index);
        mongoTemplate.indexOps("test").ensureIndex(indexDefinition);

        mongoTemplate.insert(new Document("ok", true), "test");
    }

    @AfterEach
    void cleanup() {
        mongoTemplate.dropCollection("test");
    }

    @Test
    void queryWithHintShouldWork() {
        final var query = Query.query(Criteria.where("ok").is(true));
        query.withHint(Index);
        Assertions.assertTrue(mongoTemplate.findOne(query, Document.class, "test").getBoolean("ok"));
    }

    @Test
    void updateWithHintShouldFail() {
        final var query = Query.query(Criteria.where("ok").is(true));
        query.withHint(Index);
        // fail with npe
        Assertions.assertThrows(NullPointerException.class, () -> mongoTemplate.updateFirst(query, Update.update("update_ok", true), "test"));
        Assertions.assertThrows(NullPointerException.class, () -> mongoTemplate.updateMulti(query, Update.update("update_ok", true), "test"));
    }
}
