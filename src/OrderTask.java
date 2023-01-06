import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTask implements Runnable {
    String folderPath, inProductsPath, inOrdersPath;
    ExecutorService ordersPool, productsPool;
    AtomicInteger ordersInQueue, productsInQueue;
    int orderIndex;
    String name;
    int nrProducts;
    List<Integer> shippedList = Collections.synchronizedList(new ArrayList<>());
    BufferedReader reader;
    BufferedWriter ordersWriter, productsWriter;
    Semaphore semaphore;
    int nrThreads;

    public OrderTask(String folderPath, ExecutorService ordersPool, ExecutorService productsPool,
                     AtomicInteger ordersInQueue, AtomicInteger productsInQueue, int orderIndex,
                     BufferedWriter ordersWriter, BufferedWriter productsWriter,
                     int nrThreads) throws IOException {
        this.folderPath = folderPath;
        this.ordersPool = ordersPool;
        this.productsPool = productsPool;
        this.ordersInQueue = ordersInQueue;
        this.productsInQueue = productsInQueue;
        this.ordersWriter = ordersWriter;
        this.productsWriter = productsWriter;
        this.orderIndex = orderIndex;
        this.inProductsPath = folderPath + "/order_products.txt";
        this.inOrdersPath = folderPath + "/orders.txt";
        this.nrThreads = nrThreads;
    }

    @Override
    public void run() {
        try {
            File inputFile = new File(inOrdersPath);
            reader = new BufferedReader(new FileReader(inputFile));

            // Read file line by line, until desired index is reached
            String line;
            int currIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (currIndex == orderIndex) {
                    // Process this order
                    String[] lineWords = line.split(",");
                    this.name = lineWords[0];
                    this.nrProducts = Integer.parseInt(lineWords[1]);

                    if (nrProducts != 0) {
                        // Set up the semaphore to wait for product tasks
                        semaphore = new Semaphore(1 - nrProducts);

                        // Add the product-delivery tasks to the pool
                        for (int i = 0; i < nrProducts; i++) {
                            productsInQueue.incrementAndGet();
                            productsPool.submit(new ProductTask(inProductsPath, productsPool,
                                    productsInQueue, this, i, productsWriter, semaphore));
                        }

                        // Wait for all products to be delivered
                        semaphore.acquire();

                        // Write the result to out file
                        ordersWriter.write(line + ",shipped\n");
                        ordersWriter.flush();
                    }

                    // Submit a new order task
                    ordersInQueue.incrementAndGet();
                    ordersPool.submit(new OrderTask(folderPath, ordersPool, productsPool,
                            ordersInQueue, productsInQueue,  orderIndex + nrThreads,
                            ordersWriter, productsWriter, nrThreads));
                    break;

                } else {
                    currIndex++;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // End this task and check if there are any left in the ExecutorService
        int left = ordersInQueue.decrementAndGet();
        if (left == 0) {
            ordersPool.shutdown();
        }
    }
}
