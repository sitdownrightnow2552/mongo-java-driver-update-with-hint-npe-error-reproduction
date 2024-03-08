## Spring Data Mongo - Update with hint NPE error reproduction repo

Spring Boot 3.2.3

Spring Data Mongodb 4.2.3

Mongo Driver 4.11.1

See [tests](src/test/java/com/example/npe/NpeApplicationTests.java)

### Expected behaviour

Update with `MongoTemplate` should work with `Query#withHint`

### Actual behaviour

Update fail with npe:

```
java.lang.NullPointerException: Cannot invoke "org.bson.codecs.configuration.CodecRegistry.get(java.lang.Class)" because "codecRegistry" is null
	at org.bson.Document.toBsonDocument(Document.java:134)
	at com.mongodb.internal.connection.SplittablePayload$WriteRequestEncoder.encode(SplittablePayload.java:249)
	at com.mongodb.internal.connection.SplittablePayload$WriteRequestEncoder.encode(SplittablePayload.java:182)
	at org.bson.codecs.BsonDocumentWrapperCodec.encode(BsonDocumentWrapperCodec.java:63)
	at org.bson.codecs.BsonDocumentWrapperCodec.encode(BsonDocumentWrapperCodec.java:29)
	at com.mongodb.internal.connection.BsonWriterHelper.writeDocument(BsonWriterHelper.java:77)
	at com.mongodb.internal.connection.BsonWriterHelper.writePayload(BsonWriterHelper.java:59)
	at com.mongodb.internal.connection.CommandMessage.encodeMessageBodyWithMetadata(CommandMessage.java:162)
	at com.mongodb.internal.connection.RequestMessage.encode(RequestMessage.java:136)
	at com.mongodb.internal.connection.CommandMessage.encode(CommandMessage.java:59)
	at com.mongodb.internal.connection.InternalStreamConnection.sendAndReceive(InternalStreamConnection.java:360)
	at com.mongodb.internal.connection.UsageTrackingInternalConnection.sendAndReceive(UsageTrackingInternalConnection.java:114)
	at com.mongodb.internal.connection.DefaultConnectionPool$PooledConnection.sendAndReceive(DefaultConnectionPool.java:765)
	at com.mongodb.internal.connection.CommandProtocolImpl.execute(CommandProtocolImpl.java:76)
	at com.mongodb.internal.connection.DefaultServer$DefaultServerProtocolExecutor.execute(DefaultServer.java:209)
	at com.mongodb.internal.connection.DefaultServerConnection.executeProtocol(DefaultServerConnection.java:115)
	at com.mongodb.internal.connection.DefaultServerConnection.command(DefaultServerConnection.java:83)
	at com.mongodb.internal.connection.DefaultServer$OperationCountTrackingConnection.command(DefaultServer.java:307)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.executeCommand(MixedBulkWriteOperation.java:395)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.executeBulkWriteBatch(MixedBulkWriteOperation.java:259)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.lambda$execute$2(MixedBulkWriteOperation.java:203)
	at com.mongodb.internal.operation.SyncOperationHelper.lambda$withSourceAndConnection$0(SyncOperationHelper.java:127)
	at com.mongodb.internal.operation.SyncOperationHelper.withSuppliedResource(SyncOperationHelper.java:152)
	at com.mongodb.internal.operation.SyncOperationHelper.lambda$withSourceAndConnection$1(SyncOperationHelper.java:126)
	at com.mongodb.internal.operation.SyncOperationHelper.withSuppliedResource(SyncOperationHelper.java:152)
	at com.mongodb.internal.operation.SyncOperationHelper.withSourceAndConnection(SyncOperationHelper.java:125)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.lambda$execute$3(MixedBulkWriteOperation.java:188)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.lambda$decorateWriteWithRetries$0(MixedBulkWriteOperation.java:146)
	at com.mongodb.internal.async.function.RetryingSyncSupplier.get(RetryingSyncSupplier.java:67)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.execute(MixedBulkWriteOperation.java:207)
	at com.mongodb.internal.operation.MixedBulkWriteOperation.execute(MixedBulkWriteOperation.java:77)
	at com.mongodb.client.internal.MongoClientDelegate$DelegateOperationExecutor.execute(MongoClientDelegate.java:173)
	at com.mongodb.client.internal.MongoCollectionImpl.executeSingleWriteRequest(MongoCollectionImpl.java:1085)
	at com.mongodb.client.internal.MongoCollectionImpl.executeUpdate(MongoCollectionImpl.java:1069)
	at com.mongodb.client.internal.MongoCollectionImpl.updateOne(MongoCollectionImpl.java:586)
	at org.springframework.data.mongodb.core.MongoTemplate.lambda$doUpdate$21(MongoTemplate.java:1752)
	at org.springframework.data.mongodb.core.MongoTemplate.execute(MongoTemplate.java:601)
	at org.springframework.data.mongodb.core.MongoTemplate.doUpdate(MongoTemplate.java:1725)
	at org.springframework.data.mongodb.core.MongoTemplate.updateFirst(MongoTemplate.java:1648)
```

which caused by this line:

```
// com.mongodb.internal.connection.SplittablePayload

if (update.getHint() != null) {
    writer.writeName("hint");
    // Debug show that update.getHint() return a Document
    BsonDocument hint = assertNotNull(update.getHint()).toBsonDocument(BsonDocument.class, null);
    getCodec(hint).encode(writer, hint, EncoderContext.builder().build());
}
```
