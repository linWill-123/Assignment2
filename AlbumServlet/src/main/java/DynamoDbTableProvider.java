import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class DynamoDbTableProvider {
    private static volatile DynamoDbTable<Album> albumTable;

    public static DynamoDbTable<Album> getAlbumTable() {
        if (albumTable == null) {
            synchronized (DynamoDbTableProvider.class) {
                if (albumTable == null) {
                    albumTable = DynamoDbClientProvider.getEnhancedClient()
                            .table("Albums", TableSchema.fromBean(Album.class));
                }
            }
        }
        return albumTable;
    }
}
