package com;

//import com.utilities.InventorySimulator;

import com.exceptions.OrderNotFoundException;
import com.exceptions.ProductNotFoundException;
import com.utilities.InventorySimulator;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException, InterruptedException, OrderNotFoundException, ProductNotFoundException {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n==== Inventory Management System ====");
            System.out.println("1. Manage Suppliers");
            System.out.println("2. Manage Products");
            System.out.println("3. Manage Orders");
            System.out.println("4. Manage Order Items");
            System.out.println("5. View Inventory Logs");
            System.out.println("6. Simulate Concurrent Orders");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> Supplier.runSupplier();
                case 2 -> Product.runProduct();
                case 3 -> Order.runOrder();
                case 4 -> OrderItem.runOrderItem();
                case 5 -> InventoryLog.runInventoryLog();
                case 6 -> InventorySimulator.runSimulation(); // InventorySimulator.main(null); // launch concurrent simulation
//                case 7 -> {
//                    Scanner sc = new Scanner(System.in);
//
//                    System.out.print("Enter Product ID: ");
//                    int productId = sc.nextInt();
//
//                    Product product = Product.getProductById(productId);
//
//                    if (product == null) {
//                        System.out.println("⚠ Product not found in database.");
//                        break;
//                    }
//
//                    System.out.println("Product found: " + product.getName() + " | Stock: " + product.getStockQuantity());
//
//                    System.out.print("Enter quantity to sell: ");
//                    int quantityToSell = sc.nextInt();
//
//                    try {
//                        product.sellProduct(quantityToSell);
//                        System.out.println("✅ Product sold successfully!");
//                    } catch (OutOfStockException e) {
//                        System.out.println("⚠ Error: " + e.getMessage());
//                    }
//                }
//                case 8 -> {
//                    System.out.print("Enter stock threshold: ");
//                    int threshold = scanner.nextInt();
//                    Product.checkLowStock(threshold);
//                }
                case 0 -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
        System.out.println("Exiting Inventory System.");
    }
}