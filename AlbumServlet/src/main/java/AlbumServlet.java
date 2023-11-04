import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@WebServlet(name = "AlbumServlet", value = "/albums")
@MultipartConfig
public class AlbumServlet extends HttpServlet {

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

//    try {
//      AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
//      AwsCredentials credentials = credentialsProvider.resolveCredentials();
//      System.out.println("Access Key ID: " + credentials.accessKeyId());
//      // Do not print the secret access key or session token in a real-world application
//    } catch (SdkClientException e) {
//      e.printStackTrace();
//    }

//    // Perform initialization here
    DynamoDbClient dynamoDbClient = DynamoDbClientProvider.getDynamoDbClient();
    DynamoDbEnhancedClient enhancedClient = DynamoDbClientProvider.getEnhancedClient();

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Ensure the content type is multipart/form-data
    if (!request.getContentType().startsWith("multipart/form-data")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid content type");
      return;
    }


    byte[] image = null;
    Profile profile = null;
    String artist = null;
    String title = null;
    String year = null;

    try {
      // Handle uploaded file
      Part imagePart = request.getPart("image");
      if (imagePart != null) {
        image = new byte[(int) imagePart.getSize()];
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("POST Request body missing image field");
        return;
      }
      // Handle profile field
      Part profilePart = request.getPart("profile");
      if (profilePart != null) {
        // Read its input json string into input stream
        byte[] input = new byte[(int) profilePart.getSize()];
        profilePart.getInputStream().read(input);
        String profileContent = new String(input, StandardCharsets.UTF_8);
          JsonObject profileJson =  JsonParser.parseString(profileContent).getAsJsonObject();
          profile = new Profile(
              profileJson.get("artist").getAsString(),
              profileJson.get("title").getAsString(),
              profileJson.get("year").getAsString());
        } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("POST Request body missing profile field");
        return;
      }
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Error parsing form data");
      return;
    }
    // Create album, but we aren't going to add it to our AlbumStore
    String albumID = UUID.randomUUID().toString();
    Album album = new Album(albumID, profile, image);


    // Output response with album key
    JsonObject jsonResponse = new JsonObject();
    DynamoDbTableManager.putAlbum(album);

    jsonResponse.addProperty("albumID",albumID);
    jsonResponse.addProperty("imageSize", String.valueOf(image.length));
    // Convert JsonObject to String and write to response
    response.getWriter().write(jsonResponse.toString());
  }
}
