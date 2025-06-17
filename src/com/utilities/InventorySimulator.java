package com.utilities; // This tells Java which folder this file belongs to.

import com.Product;      // We need to use the Product class.
import com.InventoryLog; // We need to use the InventoryLog class for recording changes.

import java.sql.Connection;        // Used to connect to our database.
import java.sql.PreparedStatement; // Used for running SQL commands safely.
import java.sql.ResultSet;         // Used to store results from database queries.
import java.sql.SQLException;      // This helps us catch errors that happen with the database.
import java.sql.Statement;         // Another way to run SQL commands.
import java.sql.Timestamp;         // Used for recording the exact time of an event.

import java.util.Random;           // Used to generate random numbers (for simulating delays).
// Note: We don't need 'java.util.*' if we list each specific class. It's better to be specific.

import java.util.concurrent.ExecutorService; // A tool to manage groups of threads.
import java.util.concurrent.Executors;     // Helps create different types of ExecutorService.
import java.util.concurrent.TimeUnit;      // Used to specify time units (like seconds) when waiting for threads.

/**
 * This class is designed to help us test how our inventory system works when
 * many "orders" happen at the same time. It uses Java "threads" to do this.
 *
 * Imagine a thread like a tiny, independent worker inside your program.
 * Each worker can do a task, and many workers can run at the same time!
 *
 * But if many workers try to change the *same thing* (like product stock)
 * at the *exact same time*, things can get messy. This simulator shows
 * how to handle that using "synchronization."
 */
public class InventorySimulator {

    /**
     * This is a special inner class called 'OrderProcessor'.
     * Think of it as a blueprint for our "worker" threads.
     * Each 'OrderProcessor' object will represent one order trying to buy a product.
     *
     * It implements 'Runnable', which means it knows how to run a task in a thread.
     */
    static class OrderProcessor implements Runnable {
        private int productId;     // Which product this order is for.
        private int orderQuantity; // How many items of the product are being ordered.
        private String threadName; // A name to easily identify which worker thread is doing what.

        // This is the constructor: it's called when you create a new OrderProcessor.
        public OrderProcessor(int productId, int orderQuantity, String threadName) {
            this.productId = productId;
            this.orderQuantity = orderQuantity;
            this.threadName = threadName;
        }

        /**
         * This is the heart of our worker thread. When you start a thread that
         * uses this 'OrderProcessor', the 'run()' method is what it will execute.
         */
        @Override
        public void run() {
            System.out.println(threadName + ": Attempting to process order for Product ID " + productId + " with Quantity: " + orderQuantity);
            try {
                // This is the most important part! We call a method that will safely
                // update the product stock. We'll explain 'synchronized' below.
                updateProductStockSynchronized(productId, orderQuantity, threadName);
            } catch (SQLException e) {
                // If there's a problem connecting to or using the database, we catch it here.
                System.err.println(threadName + ": Oh no! A database error happened: " + e.getMessage());
            } catch (InterruptedException e) {
                // If our thread was told to stop while it was doing something (like sleeping), we catch it here.
                System.err.println(threadName + ": Oops! My task was interrupted: " + e.getMessage());
                // This line just makes sure Java knows this thread was interrupted.
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * This method is responsible for changing the stock quantity of a product
     * in the database.
     *
     * The keyword 'synchronized' here is super important!
     * It means: "Only ONE thread can run this entire method at any given time."
     * If Thread A is inside this method, Thread B (and C, D, etc.) has to wait
     * patiently until Thread A finishes. This prevents multiple threads from
     * reading the stock, doing calculations, and writing back at the same time,
     * which could lead to wrong stock numbers. This is how we ensure "synchronized updates."
     *
     * @param productId The ID number of the product we want to change.
     * @param quantityToDecrement How many items to remove from the stock.
     * @param threadName The name of the worker thread calling this method (for logging).
     * @throws SQLException If there's any problem with the database (e.g., connection lost).
     * @throws InterruptedException If the thread waiting for a small delay gets interrupted.
     */
    public static synchronized void updateProductStockSynchronized(int productId, int quantityToDecrement, String threadName)
            throws SQLException, InterruptedException {

        // SQL commands we'll use:
        String selectSql = "SELECT stock_quantity FROM Product WHERE product_id = ?"; // Get current stock
        String updateSql = "UPDATE Product SET stock_quantity = ? WHERE product_id = ?"; // Update stock

        // These variables will hold our database connection and commands.
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection(); // Get a connection to our database.
            conn.setAutoCommit(false); // IMPORTANT: This starts a "transaction."
            // A transaction means all database changes between here and 'commit()'
            // or 'rollback()' are treated as one single, all-or-nothing operation.
            // If any part fails, everything is undone.

            // 1. First, we need to find out how much stock is currently available.
            selectStmt = conn.prepareStatement(selectSql); // Prepare the "select" command.
            selectStmt.setInt(1, productId);              // Tell it which product ID we are looking for.
            rs = selectStmt.executeQuery();               // Run the command and get the results.

            int currentStock = 0;
            if (rs.next()) { // If we found the product...
                currentStock = rs.getInt("stock_quantity"); // ...get its current stock.
            } else { // If the product ID doesn't exist...
                System.out.println(threadName + ": Product ID " + productId + " not found. Cannot process order.");
                conn.rollback(); // Undo any changes (though none were made yet, it's good practice).
                return; // Stop here, no need to continue.
            }

            System.out.println(threadName + ": Current stock for Product ID " + productId + ": " + currentStock);

            // 2. Now, let's check if we have enough stock to fulfill this order.
            if (currentStock >= quantityToDecrement) {
                int newStock = currentStock - quantityToDecrement; // Calculate the new stock level.

                // Simulate some thinking or processing time.
                // This makes it more likely for threads to try to access the same data
                // at similar times, highlighting why 'synchronized' is needed.
                Thread.sleep(new Random().nextInt(100)); // Sleep for a random time (up to 99 milliseconds).

                // 3. Update the stock in the database.
                updateStmt = conn.prepareStatement(updateSql); // Prepare the "update" command.
                updateStmt.setInt(1, newStock);              // Set the new stock quantity.
                updateStmt.setInt(2, productId);             // Tell it which product to update.
                int rowsAffected = updateStmt.executeUpdate(); // Run the update command.

                if (rowsAffected > 0) { // If the update was successful (at least one row changed)...
                    System.out.println(threadName + ": Successfully updated stock for Product ID " + productId + ". New Stock: " + newStock);
                    // 4. Record this change in the InventoryLog.
                    // We create a new log entry.
                    InventoryLog log = new InventoryLog(0, productId, -quantityToDecrement, "Order Processed by " + threadName, new Timestamp(System.currentTimeMillis()));
                    // We use an internal method to add the log using the *same* database connection
                    // that we're currently using for the transaction.
                    createLogInternal(conn, log);
                    conn.commit(); // GREAT! All changes are good, so we "save" them permanently to the database.
                } else { // If no rows were updated (something went wrong, though unlikely here)...
                    System.out.println(threadName + ": Failed to update stock for Product ID " + productId + ". No rows affected.");
                    conn.rollback(); // Undo any changes made so far in this transaction.
                }
            } else { // If there isn't enough stock...
                System.out.println(threadName + ": Insufficient stock for Product ID " + productId + ". Requested: " + quantityToDecrement + ", Available: " + currentStock);
                conn.rollback(); // Undo any changes (again, none were made, but good practice).
            }
        } catch (SQLException e) {
            // If any database error happened in the 'try' block...
            if (conn != null) {
                try {
                    System.err.println(threadName + ": Transaction failed due to SQL error. Rolling back changes.");
                    conn.rollback(); // Try to undo everything if an error occurs.
                } catch (SQLException ex) {
                    System.err.println(threadName + ": Error during rollback: " + ex.getMessage());
                }
            }
            // After trying to roll back, we re-throw the original error so the calling part knows.
            throw e;
        } finally {
            // This 'finally' block *always* runs, whether there was an error or not.
            // It's crucial for closing database resources to prevent problems.
            // We close them in reverse order of how we opened them.
            if (rs != null) { // If the ResultSet was opened...
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); } // ...close it.
            }
            if (selectStmt != null) { // If the select statement was opened...
                try { selectStmt.close(); } catch (SQLException e) { e.printStackTrace(); } // ...close it.
            }
            if (updateStmt != null) { // If the update statement was opened...
                try { updateStmt.close(); } catch (SQLException e) { e.printStackTrace(); } // ...close it.
            }
            if (conn != null) { // If the connection was opened...
                try {
                    conn.setAutoCommit(true); // Set auto-commit back to true (its default behavior).
                    conn.close();             // ...close the database connection.
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This is a helper method to create an inventory log entry.
     * It's 'private' because only methods within this class need to use it.
     * It takes an existing database 'conn' (connection) so that the log entry
     * can be part of the same transaction as the stock update.
     *
     * @param conn The active database connection.
     * @param log The InventoryLog object to be saved.
     * @throws SQLException If a database error occurs.
     */
    private static void createLogInternal(Connection conn, InventoryLog log) throws SQLException {
        String sql = "INSERT INTO InventoryLog (product_id, change_quantity, action) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) { // Uses 'try-with-resources' to auto-close 'stmt'.
            stmt.setInt(1, log.getProductId());
            stmt.setInt(2, log.getChangeQuantity());
            stmt.setString(3, log.getAction());
            stmt.executeUpdate(); // Run the insert command.
        }
    }

    /**
     * This is the main method where our program starts running.
     * It sets up the simulation.
     */
    public static void main(String[] args) {

        // --- Step 1: Prepare our test product in the database ---
        // We want to make sure Product ID 1 exists and has a starting stock of 100.
        // If it doesn't exist, this will create it. If it does, it will just reset its stock to 100.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Product (product_id, name, description, price, stock_quantity, supplier_id) " +
                             "VALUES (1, 'Test Product', 'Description of test product', 10.00, 100, 1) " +
                             "ON DUPLICATE KEY UPDATE stock_quantity = 100")) {
            stmt.executeUpdate();
            System.out.println("Setup: Ensured Product ID 1 exists with 100 stock.");
        } catch (SQLException e) {
            System.err.println("Setup Error: Could not prepare product in database: " + e.getMessage());
            // If setup fails, we can't really run the simulation, so we might exit.
            return;
        }


        // --- Step 2: Define our simulation parameters ---
        int targetProductId = 1;      // The product we will be "ordering."
        int numberOfOrders = 10;      // How many "customers" (threads) will try to place orders.
        int quantityPerOrder = 10;    // How many items each customer tries to order.

        // --- Step 3: Create a "ThreadPool" for our workers ---
        // 'ExecutorService' is like a manager for our worker threads.
        // Instead of creating threads one by one ourselves and managing them,
        // this manager helps us create a fixed number of workers (threads)
        // and gives them tasks to do. It's very efficient!
        ExecutorService executor = Executors.newFixedThreadPool(numberOfOrders);

        System.out.println("\n--- Starting Concurrent Inventory Update Simulation ---");
        System.out.println("We will try to order from Product ID: " + targetProductId);
        System.out.println("Number of simulated orders (worker threads): " + numberOfOrders);
        System.out.println("Each order will try to take: " + quantityPerOrder + " items\n");

        // --- Step 4: Give tasks to our worker threads ---
        for (int i = 0; i < numberOfOrders; i++) {
            // For each "order," we create an 'OrderProcessor' task.
            // Then, we give this task to our 'executor' manager.
            // The manager will then assign it to one of its available worker threads.
            executor.submit(new OrderProcessor(targetProductId, quantityPerOrder, "OrderThread-" + (i + 1)));
        }

        // --- Step 5: Shut down the manager and wait for all workers to finish ---
        executor.shutdown(); // Tell the manager: "No more new tasks, please finish what you have."
        try {
            // This line tells the main program to wait until all worker threads
            // have finished their jobs, or until 60 seconds have passed.
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("\nSome worker tasks did not finish in time. Trying to stop them forcefully.");
                executor.shutdownNow(); // If they didn't finish, force them to stop.
            }
        } catch (InterruptedException e) {
            System.err.println("The main program was interrupted while waiting for workers to finish: " + e.getMessage());
            executor.shutdownNow(); // Force stop.
            Thread.currentThread().interrupt(); // Reset interrupted status.
        }

        System.out.println("\n--- Simulation Finished ---");

        // --- Step 6: Check the final result in the database ---
        // After all orders (threads) have tried to update the stock,
        // let's see what the final stock quantity is in the database.
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT stock_quantity FROM Product WHERE product_id = " + targetProductId)) {
            if (rs.next()) {
                System.out.println("Final Stock Quantity for Product ID " + targetProductId + ": " + rs.getInt("stock_quantity"));
            }
        } catch (SQLException e) {
            System.err.println("Error reading final stock quantity from database: " + e.getMessage());
        }

        System.out.println("\n--- All Inventory Logs (most recent first) ---");
        // We can also see all the log entries that were created during the simulation.
        InventoryLog.readLogs();
    }
}
