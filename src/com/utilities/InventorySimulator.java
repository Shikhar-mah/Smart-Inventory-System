package com.utilities;

import com.InventoryLog;
import com.Order;
import com.OrderItem;
import com.Product;
import com.exceptions.OrderNotFoundException;
import com.exceptions.ProductNotFoundException;
import com.utilities.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InventorySimulator {

    private static final int NUMBER_OF_ORDERS = 5;
    private static final int TEST_PRODUCT_ID = 1; // Assuming a product with ID 1 exists
    private static final int INITIAL_STOCK = 50; // Initial stock for the test product

    public static void runSimulation() {
        System.out.println("\n==== Starting Concurrent Order Simulation ====");

        // Ensure the test product exists and has enough stock
        Product.ensureProductExists(TEST_PRODUCT_ID, INITIAL_STOCK);
        Product product = Product.getProductById(TEST_PRODUCT_ID);
        if (product == null) {
            System.err.println("Error: Test Product (ID: " + TEST_PRODUCT_ID + ") not found. Please create it first.");
            return;
        }

        System.out.println("Initial Stock for Product ID " + TEST_PRODUCT_ID + ": " + product.getStockQuantity());

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_ORDERS);
        Random random = new Random();

        for (int i = 1; i <= NUMBER_OF_ORDERS; i++) {
            final int orderIndex = i;
            // Simulate each order in a separate thread
            executor.submit(() -> {
                String customerName = "TestUser" + orderIndex;
                int quantityToOrder = random.nextInt(5) + 1; // Order 1 to 5 units

                try (Connection conn = DBConnection.getConnection()) {
                    // Create a new Order object for each simulation
                    Order order = new Order(new Date(System.currentTimeMillis()), customerName, "Pending");
                    int orderId = Order.createOrder(order);

                    if (orderId != -1) {
                        System.out.println("Order " + orderId + " created for " + customerName + " for " + quantityToOrder + " units of Product ID " + TEST_PRODUCT_ID);

                        // Synchronize on the Product class to ensure atomic stock updates
                        synchronized (Product.class) {
                            try {
                                conn.setAutoCommit(false); // Start transaction

                                Product p = Product.getProductById(TEST_PRODUCT_ID); // Fetch latest product state
                                if (p != null) {
                                    p.sellProduct(conn, quantityToOrder); // Use the synchronized sellProduct method
                                    // Log the inventory change
                                    InventoryLog.createLog(conn, new InventoryLog(
                                            0, TEST_PRODUCT_ID, -quantityToOrder,
                                            "Order Processed (Concurrent) - Order " + orderId,
                                            new Timestamp(System.currentTimeMillis())
                                    ));
                                    OrderItem.createOrderItem(new OrderItem(orderId, TEST_PRODUCT_ID, quantityToOrder, p.getPrice()));
                                    conn.commit(); // Commit transaction
                                } else {
                                    System.err.println("Product ID " + TEST_PRODUCT_ID + " not found during order processing.");
                                    conn.rollback();
                                }
                            } catch (SQLException e) {
                                conn.rollback(); // Rollback on error
                                System.err.println("Transaction failed for Order " + orderId + ": " + e.getMessage());
                            } catch (ProductNotFoundException | OrderNotFoundException e) {
                                System.err.println("Error creating order item for Order " + orderId + ": " + e.getMessage());
                            } finally {
                                conn.setAutoCommit(true); // Reset auto-commit
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Database connection error for Order " + orderIndex + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown(); // Shut down the executor after all tasks are submitted
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES); // Wait for all threads to complete
            System.out.println("\n==== Concurrent Order Simulation Finished ====");
            Product finalProductState = Product.getProductById(TEST_PRODUCT_ID);
            if (finalProductState != null) {
                System.out.println("Final Stock for Product ID " + TEST_PRODUCT_ID + ": " + finalProductState.getStockQuantity());
            }
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
    }
}