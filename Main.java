import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);
            
        System.out.println("Enter the upper limit: ");
        int limit = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        int threadCount = 1; // Default to 1 thread if user doesn't specify
        try {
            System.out.println("Enter the number of threads (leave blank for default 1): ");
            String threadInput = scanner.nextLine(); // Use nextLine() instead of next()
            if (!threadInput.isEmpty()) {
                threadCount = Integer.parseInt(threadInput);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for number of threads. Using default value of 1.");
        }

        scanner.close();

        // Create a Lock object for mutual exclusion
        Lock lock = new ReentrantLock();
        List<Integer> primes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = new ArrayList<>();

        // Start the timer
        long startTime = System.nanoTime();

        // Submit tasks to the executor
        for (int i = 0; i < threadCount; i++) {
            final int start = i * limit / threadCount + 1;
            final int end = (i == threadCount - 1)? limit : start + limit / threadCount - 1;
            futures.add(executor.submit(new PrimeFinder(start, end, lock, primes)));
        }

        // Wait for all tasks to complete
        for (Future<Void> future : futures) {
            future.get(); // This will block until the task is complete
        }

        executor.shutdown();

        // Stop the timer
        long endTime = System.nanoTime();

        // Calculate and print the elapsed time
        long duration = (endTime - startTime); // In nanoseconds
        double seconds = (double)duration / 1_000_000_000.0; // Convert to seconds
        System.out.printf("Time taken: %.6f seconds\n", seconds);

        System.out.printf("%d primes were found.\n", primes.size());
    }    

    static class PrimeFinder implements Callable<Void> {
        private final int start;
        private final int end;
        private final Lock lock;
        private final List<Integer> primes;

        PrimeFinder(int start, int end, Lock lock, List<Integer> primes) {
            this.start = start;
            this.end = end;
            this.lock = lock;
            this.primes = primes;
        }

        @Override
        public Void call() {
            for (int n = start; n <= end; n++) {
                if (check_prime(n)) {
                    lock.lock(); // Acquire the lock
                    try {
                        primes.add(n); // Safely add the prime number to the list
                    } finally {
                        lock.unlock(); // Release the lock
                    }
                }
            }
            return null;
        }
    }

    public static boolean check_prime(int n) {
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}
