import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsRunner {
    public static void runThreads(final String baseUrl, final String csvFilePath, int threadGroupSize,
                                  int numThreadGroups, int delaySeconds) throws InterruptedException, ExecutionException {

        BlockingQueue<String> queue = new LinkedBlockingDeque<>();
        CyclicBarrier barrier = new CyclicBarrier(10);
        String imagePath = "/Users/willxzy/Downloads/MicrosoftTeams-image.png";

        ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);
        List<Future<int[]>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Future<int[]> executedRes = executor.submit(new ProducerThread(baseUrl,imagePath,barrier,queue,100));
            futures.add(executedRes);
        }

        executor.shutdown();

        long testStartTime = System.currentTimeMillis();
        System.out.println(testStartTime);

        barrier = new CyclicBarrier(numThreadGroups*threadGroupSize);
        executor = Executors.newFixedThreadPool(numThreadGroups*threadGroupSize);

        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                Future<int[]> executedRes = executor.submit(new ProducerThread(baseUrl,imagePath,barrier,queue,1000));
                futures.add(executedRes);
            }
            Thread.sleep(delaySeconds * 1000);
        }

        executor.shutdown();

        writeBatchToFile(csvFilePath, queue);

        long testEndTime = System.currentTimeMillis();
        System.out.println(testEndTime);

        long wallTime = (testEndTime - testStartTime) / 1000;

        long totalSuccess = 0;
        long totalFailure = 0;

        for (int i = 0; i < futures.size(); i++) {
            int[] executedRes = futures.get(i).get();
            totalSuccess += executedRes[0];
            totalFailure += executedRes[1];
        }

        long throughput = totalSuccess / wallTime;

        System.out.println("Wall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("Successful Requests: " + totalSuccess);
        System.out.println("Failed Requests: " + totalFailure);;


    }

    private static void writeBatchToFile(String filename, BlockingQueue<String> queue) {
        List<String> batchToWrite = new ArrayList<>();
        while (!queue.isEmpty()) {
            batchToWrite.add(queue.poll());
        }

        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (String log : batchToWrite) {
                out.println(log);
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }

}
