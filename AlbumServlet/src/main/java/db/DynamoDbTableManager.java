package db;

import model.Album;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.regions.Region;

public class DynamoDbTableManager {
    private static final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();

    private static final DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();

    public static void initializeDbTable() {
        try {
            DynamoDbTable<Album> albumTable = enhancedClient.table("Albums", TableSchema.fromBean(Album.class));
            createAlbumTable(albumTable, dynamoDbClient);
        } catch (ResourceInUseException e) {
            System.err.println("Table already exists and is in use: " + e.getMessage());
        }
    }

    public static void putAlbum(Album album) {
        getAlbumTableInstance().putItem(album);
    }

    public static Album getAlbum(String albumId) {
        Key key = Key.builder().partitionValue(albumId).build();
        return getAlbumTableInstance().getItem(r -> r.key(key));
    }

    private static DynamoDbTable<Album> getAlbumTableInstance() {
        return enhancedClient.table("Albums", TableSchema.fromBean(Album.class));
    }

    private static void createAlbumTable(DynamoDbTable<Album> albumDynamoDbTable, DynamoDbClient dynamoDbClient) {
        // Create the DynamoDB table by using the 'albumDynamoDbTable' DynamoDbTable instance.
        albumDynamoDbTable.createTable(builder -> builder
                .provisionedThroughput(b -> b
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
        );
        // The 'dynamoDbClient' instance that's passed to the builder for the DynamoDbWaiter is the same instance
        // that was passed to the builder of the DynamoDbEnhancedClient instance used to create the 'albumDynamoDbTable'.
        // This means that the same Region that was configured on the standard 'dynamoDbClient' instance is used for all service clients.
        try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) { // DynamoDbWaiter is Autocloseable
            ResponseOrException<DescribeTableResponse> response = waiter
                    .waitUntilTableExists(builder -> builder.tableName("Albums").build())
                    .matched();
            DescribeTableResponse tableDescription = response.response().orElseThrow(
                    () -> new RuntimeException("Album table was not created."));
            // The actual error can be inspected in response.exception()
            System.out.println("Album table was created.");
        }
    }
}
