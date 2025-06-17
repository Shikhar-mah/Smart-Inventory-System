package com;

import com.utilities.InventorySimulator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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
                case 1 -> manageSuppliers(scanner);
                case 2 -> manageProducts(scanner);
                case 3 -> manageOrders(scanner);
                case 4 -> manageOrderItems(scanner);
                case 5 -> InventoryLog.readLogs();
                case 6 -> InventorySimulator.main(null); // launch concurrent simulation
                case 0 -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
        System.out.println("Exiting Inventory System.");
    }

    private static void manageSuppliers(Scanner sc) {
        System.out.println("1-Create | 2-Read | 3-Update | 4-Delete");
        int choice = sc.nextInt(); sc.nextLine();

        switch (choice) {
            case 1 -> {
                Supplier s = new Supplier(0, "New Supplier", "supplier@example.com", "9876543210");
                Supplier.createSupplier(s);
            }
            case 2 -> Supplier.readSuppliers();
            case 3 -> {
                System.out.print("Enter supplier ID: ");
                int id = sc.nextInt(); sc.nextLine();
                System.out.print("New name: ");
                String name = sc.nextLine();
                Supplier.updateSupplier(id, name);
            }
            case 4 -> {
                System.out.print("Enter supplier ID to delete: ");
                int id = sc.nextInt();
                Supplier.deleteSupplier(id);
            }
        }
    }

    private static void manageProducts(Scanner sc) {
        System.out.println("1-Create | 2-Read | 3-Update Price | 4-Delete");
        int choice = sc.nextInt(); sc.nextLine();

        switch (choice) {
            case 1 -> {
                Product p = new Product(0, "Keyboard", "Mechanical keyboard", 1500.00, 10, 1);
                Product.createProduct(p);
            }
            case 2 -> Product.readProducts();
            case 3 -> {
                System.out.print("Product ID: ");
                int id = sc.nextInt();
                System.out.print("New price: ");
                double price = sc.nextDouble();
                Product.updateProductPrice(id, price);
            }
            case 4 -> {
                System.out.print("Product ID to delete: ");
                int id = sc.nextInt();
                Product.deleteProduct(id);
            }
        }
    }

    private static void manageOrders(Scanner sc) {
        System.out.println("1-Create | 2-Read | 3-Update Status | 4-Delete");
        int choice = sc.nextInt(); sc.nextLine();

        switch (choice) {
            case 1 -> {
                Order o = new Order(0, new java.sql.Date(System.currentTimeMillis()), "Customer Name", "Pending");
                Order.createOrder(o);
            }
            case 2 -> Order.readOrders();
            case 3 -> {
                System.out.print("Order ID: ");
                int id = sc.nextInt(); sc.nextLine();
                System.out.print("New status: ");
                String status = sc.nextLine();
                Order.updateOrderStatus(id, status);
            }
            case 4 -> {
                System.out.print("Order ID to delete: ");
                int id = sc.nextInt();
                Order.deleteOrder(id);
            }
        }
    }

    private static void manageOrderItems(Scanner sc) {
        System.out.println("1-Create | 2-Read | 3-Update Quantity | 4-Delete");
        int choice = sc.nextInt(); sc.nextLine();

        switch (choice) {
            case 1 -> {
                OrderItem item = new OrderItem(0, 1, 1, 2, 499.99); // sample values
                OrderItem.createOrderItem(item);
            }
            case 2 -> OrderItem.readOrderItems();
            case 3 -> {
                System.out.print("Order Item ID: ");
                int id = sc.nextInt();
                System.out.print("New quantity: ");
                int qty = sc.nextInt();
                OrderItem.updateQuantity(id, qty);
            }
            case 4 -> {
                System.out.print("Order Item ID to delete: ");
                int id = sc.nextInt();
                OrderItem.deleteOrderItem(id);
            }
        }
    }
}
