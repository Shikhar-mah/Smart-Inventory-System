//package com.utilities;
//
//import com.Product;
//import com.InventoryLog;
//
//import java.sql.*;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//public class InventorySimulator {
//    static Scanner sc = new Scanner(System.in);
//    static class OrderProcessor implements Runnable {
//        private int productId;
//        private int orderQuantity;
//        private String threadName;
//
//        public OrderProcessor(int productId, int orderQuantity, String threadName) {
//            this.productId = productId;
//            this.orderQuantity = orderQuantity;
//            this.threadName = threadName;
//        }
//
//        @Override
//        public void run() {
//            System.out.println(threadName + ": Attempting to process order for Product ID " + productId + " with Quantity: " + orderQuantity);
//            try {
//                updateProductStockSynchronized(productId, orderQuantity, threadName);
//            } catch (SQLException e) {
//                System.err.println(threadName + ": database error occurred: " + e.getMessage());
//            } catch (InterruptedException e) {
//                System.err.println(threadName + ": The task was interrupted: " + e.getMessage());
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    public static synchronized void updateProductStockSynchronized(int productId, int quantityToDecrement, String threadName)
//            throws SQLException, InterruptedException {
//
//        String selectSql = "SELECT stock_quantity FROM Product WHERE product_id = ?"; // Get current stock
//        String updateSql = "UPDATE Product SET stock_quantity = ? WHERE product_id = ?"; // Update stock
//
//        Connection conn = null;
//        PreparedStatement selectStmt = null;
//        PreparedStatement updateStmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DBConnection.getConnection();
//            conn.setAutoCommit(false);
//
//            selectStmt = conn.prepareStatement(selectSql);
//            selectStmt.setInt(1, productId);
//            rs = selectStmt.executeQuery();
//
//            int currentStock = 0;
//            if (rs.next()) {
//                currentStock = rs.getInt("stock_quantity"); // ...get its current stock.
//            } else {
//                System.out.println(threadName + ": Product ID " + productId + " not found. Cannot process order.");
//                conn.rollback();
//                return;
//            }
//
//            System.out.println(threadName + ": Current stock for Product ID " + productId + ": " + currentStock);
//
//            if (currentStock >= quantityToDecrement) {
//                int newStock = currentStock - quantityToDecrement;
//
//                Thread.sleep(new Random().nextInt(100));
//
//                updateStmt = conn.prepareStatement(updateSql);
//                updateStmt.setInt(1, newStock);
//                updateStmt.setInt(2, productId);
//                int rowsAffected = updateStmt.executeUpdate();
//
//                if (rowsAffected > 0) {
//                    System.out.println(threadName + ": Successfully updated stock for Product ID " + productId + ". New Stock: " + newStock);
//
//                    InventoryLog log = new InventoryLog(0, productId, -quantityToDecrement, "Order Processed by " + threadName, new Timestamp(System.currentTimeMillis()));
//
//                    createLogInternal(conn, log);
//                    conn.commit();
//                } else {
//                    System.out.println(threadName + ": Failed to update stock for Product ID " + productId + ". No rows affected.");
//                    conn.rollback();
//                }
//            } else {
//                System.out.println(threadName + ": Insufficient stock for Product ID " + productId + ". Requested: " + quantityToDecrement + ", Available: " + currentStock);
//                conn.rollback();
//            }
//        } catch (SQLException e) {
//
//            if (conn != null) {
//                try {
//                    System.err.println(threadName + ": Transaction failed due to SQL error. Rolling back changes.");
//                    conn.rollback();
//                } catch (SQLException ex) {
//                    System.err.println(threadName + ": Error during rollback: " + ex.getMessage());
//                }
//            }
//
//            throw e;
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (selectStmt != null) {
//                try {
//                    selectStmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (updateStmt != null) {
//                try {
//                    updateStmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (conn != null) {
//                try {
//                    conn.setAutoCommit(true);
//                    conn.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private static void createLogInternal(Connection conn, InventoryLog log) throws SQLException {
//        String sql = "INSERT INTO InventoryLog (product_id, change_quantity, action) VALUES (?, ?, ?)";
//        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, log.getProductId());
//            stmt.setInt(2, log.getChangeQuantity());
//            stmt.setString(3, log.getAction());
//            stmt.executeUpdate();
//        }
//    }
//
//    public static void main(String[] args) throws SQLException {
//        try (Connection conn = DBConnection.getConnection();
//
//             PreparedStatement stmt = conn.prepareStatement(
//                     "INSERT INTO Product (product_id, name, description, price, stock_quantity, supplier_id) " +
//                             "VALUES (1, 'Test Product', 'Description of test product', 10.00, 20, 1) " +
//                             "ON DUPLICATE KEY UPDATE stock_quantity = 20")) {
//            stmt.executeUpdate();
//            System.out.println("Setup: Ensured Product ID 1 exists with 100 stock.");
//        } catch (SQLException e) {
//            System.err.println("Setup Error: Could not prepare product in database: " + e.getMessage());
//            return;
//        }
//
//        int targetProductId = 1;
//        int numberOfOrders = 5;
//        int quantityPerOrder = 7;
//
//        List<Thread> workerThreads = new ArrayList<>();
//
//        System.out.println("\n--- Starting Concurrent Inventory Update Simulation ---");
//        System.out.println("We will try to order from Product ID: " + targetProductId);
//        System.out.println("Number of simulated orders (worker threads): " + numberOfOrders);
//        System.out.println("Each order will try to take: " + quantityPerOrder + " items\n");
//
//        for (int i = 0; i < numberOfOrders; i++) {
//            OrderProcessor task = new OrderProcessor(targetProductId, quantityPerOrder, "OrderThread-" + (i + 1));
//
//            Thread worker = new Thread(task);
//
//            workerThreads.add(worker);
//
//            worker.start();
//
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                System.err.println("Main thread interrupted while pausing between starting workers: " + e.getMessage());
//                Thread.currentThread().interrupt(); // Restore the interrupted status.
//            }
//
//        }
//
//        for (Thread worker : workerThreads) {
//            try {
//                worker.join();
//            } catch (InterruptedException e) {
//                System.err.println("The main program was interrupted while waiting for a worker to finish: " + e.getMessage());
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        System.out.println("\n--- Simulation Finished ---");
//
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT stock_quantity FROM Product WHERE product_id = " + targetProductId)) {
//            if (rs.next()) {
//                System.out.println("Final Stock Quantity for Product ID " + targetProductId + ": " + rs.getInt("stock_quantity"));
//            }
//        } catch (SQLException e) {
//            System.err.println("Error reading final stock quantity from database: " + e.getMessage());
//        }
//
//        System.out.println("\n--- All Inventory Logs (most recent first) ---");
//        InventoryLog.readLogs();
//    }
//}
