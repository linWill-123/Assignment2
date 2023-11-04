import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

public class AlbumRepository {

    public void putAlbum(Album album) {
        DynamoDbTable<Album> albumTable = DynamoDbTableProvider.getAlbumTable();
        // Assuming that putItem is thread-safe from the SDK
        albumTable.putItem(album);
    }

    public Album getAlbum(String albumId) {
        DynamoDbTable<Album> albumTable = DynamoDbTableProvider.getAlbumTable();
        Key key = Key.builder().partitionValue(albumId).build();
        // Assuming that getItem is thread-safe from the SDK
        return albumTable.getItem(r -> r.key(key));
    }

}
