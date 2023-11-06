import com.squareup.okhttp.Call;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.awt.*;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class ProducerThread implements Callable<int[]> {
    private int numSuccess;
    private int numFailure;

    private int numIterations;
    private DefaultApi apiInstance;
    private String imagePath;

    private BlockingQueue<String> queue;

    public ProducerThread(String serverUrl, String imagePath,  BlockingQueue<String> queue, int numIterations) {
        ApiClient client = new ApiClient();
        client.setBasePath(serverUrl);
        this.apiInstance = new DefaultApi(client);
        this.imagePath = imagePath;
        this.queue = queue;
        this.numIterations = numIterations;
    }

    @Override
    public int[] call() {
        File image = new File(imagePath);
        AlbumsProfile profile = new AlbumsProfile();
        profile.setArtist("Eminem");
        profile.setTitle("MMlp2");
        profile.setYear("2001");

        for (int i = 0; i < numIterations; i++) {
            ImageMetaData postResponse = doPost(image,profile);
            if (postResponse != null) {
                doGet(postResponse.getAlbumID());
            }
        }

        return new int[] {numSuccess,numFailure};
    }

    public ImageMetaData doPost(File image, AlbumsProfile profile) {
        long latency = 0;
        long startTimestamp = System.currentTimeMillis();
        ImageMetaData postResponse = null;
        // POST latency calculation
        try {
            postResponse = apiInstance.newAlbum(image, profile); // Call Get
            latency = System.currentTimeMillis() - startTimestamp;
            queue.add(startTimestamp + "," + "POST" + "," + latency+ "," + 200);
            numSuccess++;

        } catch (ApiException e) {
            latency = System.currentTimeMillis() - startTimestamp;
            queue.add(startTimestamp + "," + "POST"+ "," + latency+ "," + e.getCode());
            System.err.println(e.getMessage());
            numFailure++;
        }
        return postResponse;
    }

    public void doGet(String albumId) {
        long latency = 0;
        long startTimestamp = System.currentTimeMillis();
        // POST latency calculation
        try {
            AlbumInfo getResponse = apiInstance.getAlbumByKey(albumId); // Call Get
            latency = System.currentTimeMillis() - startTimestamp;
            queue.add(startTimestamp + "," + "GET"+ "," + latency + "," + 200);
            numSuccess++;

        } catch (ApiException e) {
            latency = System.currentTimeMillis() - startTimestamp;
            queue.add(startTimestamp + "," + "GET"+ "," + latency+ "," + e.getCode());
            System.err.println(e.getMessage());
            numFailure++;
        }
    }

}
