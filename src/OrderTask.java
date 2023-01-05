import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTask implements Runnable {
    String folderPath, inOrdersPath, inProductsPath;
    ExecutorService ordersPool, productsPool;
    AtomicInteger ordersInQueue, productsInQueue;
    int orderIndex;
    String name;
    int nrProducts;
    List<Integer> shippedList = Collections.synchronizedList(new ArrayList<>());
    BufferedReader reader;
    BufferedWriter ordersWriter, productsWriter;

    public OrderTask(String folderPath, ExecutorService ordersPool, ExecutorService productsPool,
                     AtomicInteger ordersInQueue, AtomicInteger productsInQueue, int orderIndex,
                     BufferedWriter ordersWriter, BufferedWriter productsWriter) throws IOException {
        this.folderPath = folderPath;
        this.ordersPool = ordersPool;
        this.productsPool = productsPool;
        this.ordersInQueue = ordersInQueue;
        this.productsInQueue = productsInQueue;
        this.ordersWriter = ordersWriter;
        this.productsWriter = productsWriter;
        this.orderIndex = orderIndex;
        this.inOrdersPath = folderPath + "/orders.txt";
        this.inProductsPath = folderPath + "/order_products.txt";
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

                    // Start a product-delivery task
                    productsInQueue.incrementAndGet();
                    productsPool.submit(new ProductTask(inProductsPath, productsPool, productsInQueue,
                            this, 0, productsWriter));

                    // Submit next order
                    ordersInQueue.incrementAndGet();
                    ordersPool.submit(new OrderTask(folderPath, ordersPool, productsPool,
                            ordersInQueue, productsInQueue,  orderIndex + 1, ordersWriter,
                            productsWriter));
                    break;

                } else {
                    currIndex++;
                }
            }

            // Wait for all products to be shipped
            while (shippedList.size() != nrProducts) {
                wait();
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
