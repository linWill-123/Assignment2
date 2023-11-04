import com.squareup.okhttp.Call;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public class ProducerThread implements Callable<int[]> {
    private int numSuccess;
    private int numFailure;

    private int numIterations;
    private DefaultApi apiInstance;
    private String imagePath;

    private CyclicBarrier barrier;
    private BlockingQueue<String> queue;

    public ProducerThread(String serverUrl, String imagePath, CyclicBarrier barrier, BlockingQueue<String> queue, int numIterations) {
        ApiClient client = new ApiClient();
        client.setBasePath(serverUrl);
        this.apiInstance = new DefaultApi(client);
        this.imagePath = imagePath;
        this.barrier = barrier;
        this.queue = queue;
        this.numIterations = numIterations;
    }

    public void doPost(File image, AlbumsProfile profile) {
        long latency = 0;
        long startTimestamp = System.currentTimeMillis();
        // POST latency calculation
        try {
            ImageMetaData postResponse = apiInstance.newAlbum(image, profile); // Call Get
            latency = System.currentTimeMillis() - startTimestamp;
            queue.add(startTimestamp + "," + "POST"+ "," + latency+ "," + 200);
            numSuccess++;

        } catch (ApiException e) {
            latency = System.currentTimeMillis() - startTimestamp;
            queue.add(startTimestamp + "," + "POST"+ "," + latency+ "," + e.getCode());
            System.err.println(e.getMessage());
            numFailure++;
        }
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

    @Override
    public int[] call() {
        File image = new File(imagePath);
        AlbumsProfile profile = new AlbumsProfile();
        profile.setArtist("Eminem");
        profile.setTitle("MMlp2");
        profile.setYear("2001");

        for (int i = 0; i < numIterations; i++) {
            doPost(image,profile);
            doGet("1");
        }

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        return new int[] {numSuccess,numFailure};
    }
}
