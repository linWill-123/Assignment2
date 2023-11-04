import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.regions.Region;

public class DynamoDbTableManager {
    public static final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();

    public static final DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();

    private static final DynamoDbTable<Album> albumDynamoDbTable = null;

    public static DynamoDbTable<Album> getAlbumTableInstance() {
        DynamoDbTable<Album> albumTable = enhancedClient.table("Albums", TableSchema.fromBean(Album.class));

        /* Since albumTable instance doesn't know if the table is created in dynamodb,
           we check if the table is created, if not we will create the table in the client */

        if (!doesTableExist("Albums")) {
            createAlbumTable(albumTable,dynamoDbClient);
        }

        return albumTable;
    }

    public static void putAlbum(Album album) {
        try {
            getAlbumTableInstance().putItem(album);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Album getAlbum(String albumId) {
        Key key = Key.builder().partitionValue(albumId).build();
        return getAlbumTableInstance().getItem(r -> r.key(key));
    }

    private static boolean doesTableExist(String tableName) {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
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
