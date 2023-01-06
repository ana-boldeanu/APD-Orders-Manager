import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    public static void main(String[] args) throws IOException {
        // Get input data
        String folderPath = args[0];
        int nrThreads = Integer.parseInt(args[1]);

        // Set up the File Writers
        String outOrdersPath = "orders_out.txt";
        String outProductsPath = "order_products_out.txt";

        BufferedWriter ordersWriter = new BufferedWriter(new FileWriter(outOrdersPath));
        BufferedWriter productsWriter = new BufferedWriter(new FileWriter(outProductsPath));

        // Set up the Executor pools
        AtomicInteger ordersInQueue = new AtomicInteger(0);
        AtomicInteger productsInQueue = new AtomicInteger(0);

        ExecutorService ordersPool = Executors.newFixedThreadPool(nrThreads);
        ExecutorService productsPool = Executors.newFixedThreadPool(nrThreads);

        // Start the order tasks
        for (int i = 0; i < nrThreads; i++) {
            ordersInQueue.incrementAndGet();
            ordersPool.submit(new OrderTask(folderPath, ordersPool, productsPool,
                    ordersInQueue, productsInQueue, ordersWriter, productsWriter,
                    nrThreads, i));
        }
    }
}
