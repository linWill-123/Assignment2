import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;


public class DynamoDbClientProvider {
    private static final DynamoDbClient DYNAMO_DB_CLIENT;
    private static final DynamoDbEnhancedClient ENHANCED_CLIENT;

    static {
        try {
            DYNAMO_DB_CLIENT = DynamoDbClient.builder()
                    .region(Region.US_WEST_2)
                    .build();
            ENHANCED_CLIENT = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(DYNAMO_DB_CLIENT)
                    .build();
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize DynamoDB clients", e);
        }
    }

    public static DynamoDbClient getDynamoDbClient() {
        return DYNAMO_DB_CLIENT;
    }

    public static DynamoDbEnhancedClient getEnhancedClient() {
        return ENHANCED_CLIENT;
    }
}
