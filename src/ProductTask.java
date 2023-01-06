import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductTask implements Runnable {
    String inputPath;
    ExecutorService productsPool;
    AtomicInteger inQueue;
    OrderTask orderTask;
    int productIndex;
    BufferedReader reader;
    BufferedWriter writer;
    Semaphore semaphore;

    public ProductTask(String inputPath, ExecutorService productsPool, AtomicInteger inQueue,
                       OrderTask orderTask, int productIndex, BufferedWriter writer,
                       Semaphore semaphore) {
        this.inputPath = inputPath;
        this.productsPool = productsPool;
        this.inQueue = inQueue;
        this.orderTask = orderTask;
        this.productIndex = productIndex;
        this.writer = writer;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            File inputFile = new File(inputPath);
            reader = new BufferedReader(new FileReader(inputFile));

            // Read file line by line, looking for the product with the given index
            String line;
            int foundProducts = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(orderTask.name)) {
                    if (foundProducts == productIndex) {
                        // Deliver this product
                        writer.write(line + ",shipped\n");
                        writer.flush();

                        // Mark product as shipped and notify the order manager
                        orderTask.shippedList.add(productIndex);
                        break;

                    } else {
                        foundProducts++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // End this task and check if there are any left in the ExecutorService
        semaphore.release();

        int left = inQueue.decrementAndGet();
        if (left == 0) {
            productsPool.shutdown();
        }
    }
}
