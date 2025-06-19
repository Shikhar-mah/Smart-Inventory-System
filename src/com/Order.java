package com;

import com.exceptions.OrderNotFoundException;
import com.exceptions.ProductNotFoundException;
import com.utilities.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Order {
    private int orderId;
    private Date orderDate;
    private String customerName;
    private String status;

    static Scanner sc = new Scanner(System.in);

    // Constructors
    public Order() {
    }

    public Order(Date orderDate, String customerName, String status) {
//        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customerName = customerName;
        this.status = status;
    }

    public Order(int orderId, Date orderDate, String customerName, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customerName = customerName;
        this.status = status;
    }

    // Getters & Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate +
                ", customerName='" + customerName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    // ---------- JDBC CRUD Methods ------------

    public static int createOrder(Order order) {
        String sql = "INSERT INTO Orders (order_date, customer_name, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, order.getOrderDate());
            stmt.setString(2, order.getCustomerName());
            stmt.setString(3, order.getStatus());

            int affectedRows = stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return new orderId
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void createOrderWithItems(Order order) throws SQLException, InterruptedException, OrderNotFoundException, ProductNotFoundException {
        int orderId = createOrder(order);
        if (orderId == -1) {
            System.out.println("Failed to create order.");
            return;
        }

        List<OrderItem> itemsForOrder = new ArrayList<>();
        System.out.println("Add items for | Name: " + order.getCustomerName() + " | OrderId: " + orderId);

        Scanner sc = new Scanner(System.in);

        while (true) {
            int prodId = 0;
            boolean validProductId = false;

            // Product ID input with exception handling
            Product.readProducts();
            while (!validProductId) {
                System.out.print("Enter Product ID: ");

                if (!sc.hasNextInt()) {
                    System.out.println("Please enter a numeric product ID!");
                    sc.next(); // Clear invalid input
                    continue;
                }

                prodId = sc.nextInt();

                try {
                    if (OrderItem.productExists(prodId)) {
                        validProductId = true;
                    } else {
                        throw new ProductNotFoundException("Product with ID " + prodId + " does not exist.");
                    }
                } catch (ProductNotFoundException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Please try again.");
                }
            }

            // Quantity input validation
            int quantity = 0;
            boolean validQuantity = false;

            while (!validQuantity) {
                System.out.print("Enter Quantity: ");

                if (!sc.hasNextInt()) {
                    System.out.println("Please enter a numeric quantity!");
                    sc.next(); // Clear invalid input
                    continue;
                }

                quantity = sc.nextInt();

                if (quantity > 0) {
                    validQuantity = true;
                } else {
                    System.out.println("Quantity must be greater than 0!");
                }
            }

            // Process product and stock
            Product product = Product.getProductById(prodId);

//            if (product == null) {
//                System.out.println("Error: Product not found!");
//                continue;
//            }

            if (product.getStockQuantity() < quantity) {
                System.out.println("Error: Not enough stock available! Current stock: " + product.getStockQuantity());
                continue;
            }

            // Create order item
            double prodPrice = product.getPrice() * quantity;
            OrderItem item = new OrderItem(orderId, prodId, quantity, prodPrice);
            itemsForOrder.add(item);

            // Update stock with transaction
            Connection conn = DBConnection.getConnection();
            try {
                conn.setAutoCommit(false);

                int newStock = product.getStockQuantity() - quantity;
                Product.updateProductStock(conn, prodId, newStock);

                InventoryLog.createLog(conn, new InventoryLog(
                        0, prodId, -quantity,
                        "Order Processed (Manual)",
                        new Timestamp(System.currentTimeMillis())
                ));

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Failed to update stock: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }

            // Ask to add more items
            System.out.println("Add more items?");
            System.out.println("1. Yes");
            System.out.println("2. No");

            int choice;
            while (true) {
                System.out.print("Your choice (1/2): ");

                if (!sc.hasNextInt()) {
                    System.out.println("Please enter 1 or 2!");
                    sc.next(); // Clear invalid input
                    continue;
                }

                choice = sc.nextInt();

                if (choice == 1 || choice == 2) break;
                System.out.println("Please enter 1 or 2!");
            }

            if (choice == 2) break;
        }

        for(OrderItem oi: itemsForOrder){
            OrderItem.createOrderItem(oi);
        }

        System.out.println("Order created successfully with " + itemsForOrder.size() + " item(s).");



    }


    public static void readOrders() {
        String sql = "SELECT * FROM Orders";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-10s %-15s %-25s %-10s%n", "Order ID", "Order Date", "Customer Name", "Status");
            System.out.println("---------------------------------------------------------------");

            while (rs.next()) {
                Order o = new Order(
                        rs.getInt("order_id"),
                        rs.getDate("order_date"),
                        rs.getString("customer_name"),
                        rs.getString("status")
                );

                System.out.printf(
                        "%-10d %-15s %-25s %-10s%n",
                        o.getOrderId(),
                        o.getOrderDate().toString(),
                        o.getCustomerName(),
                        o.getStatus()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrderStatusChoice(int id, int st) {
        String status = "";
        switch (st) {
            case 1:
                status = "Pending";
                break;
            case 2:
                status = "Completed";
                break;
            default:
                System.out.println("Invalid choice. Status not updated.");
                break;
        }
        if (!status.isEmpty()) {
            updateOrderStatus(id, status);
        }
    }

    public static void updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);

            int rows = stmt.executeUpdate();
            System.out.println(rows + " order(s) updated.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrder(int orderId) {
        String sql = "DELETE FROM Orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " order(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------ Optional main() for Testing ------------
    public static void runOrder() throws InterruptedException, OrderNotFoundException, ProductNotFoundException, SQLException {
        Scanner sc = new Scanner(System.in);
//        System.out.println("Choose: 1-Create | 2-Read | 3-Update Status | 4-Delete");
//        int choice = sc.nextInt();
//        sc.nextLine();
//
//        switch (choice) {
//            case 1:
//                System.out.println("----Enter the Details----");
////                System.out.println("orderId: ");
////                int orderId = sc.nextInt();
//
//                System.out.println("customerName: ");
//                String custName = sc.next();
//
////                System.out.println("Status: ");  // make changes here
//                String status_default = "Pending";
//
//                Order newOrder = new Order(new Date(System.currentTimeMillis()), custName, status_default);
//                createOrderWithItems(newOrder);
//                break;
//            case 2:
//                readOrders();
//                break;
//            case 3:
//                System.out.print("Order ID: ");
//                int id = sc.nextInt();
//                sc.nextLine();
//                System.out.println("Select New Status:");
//                System.out.println("1-Pending | 2-Completed ");
//                int st = sc.nextInt();
//                sc.nextLine();
//                updateOrderStatusChoice(id, st);
//                break;
//            case 4:
//                System.out.print("Order ID to delete: ");
//                int delId = sc.nextInt();
//                deleteOrder(delId);
//                break;
//        }
        int choice = -1;
        boolean validInput = false;

        while (!validInput) {
            System.out.println("Choose: 1-Create | 2-Read | 3-Update Status | 4-Delete | 5-Exit");
            String input = sc.nextLine();

            // Check if input is letters only (a-z or A-Z)
            if (input.matches("[a-zA-Z]+")) {
                System.out.println("Invalid, please enter a valid number 1-5");
                continue;
            }

            try {
                choice = Integer.parseInt(input);

                if (choice < 1 || choice > 5) {
                    System.out.println("Invalid input, please choose 1-5");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid, please enter a valid number 1-5");
            }
        }

//        switch (choice) {
//            case 1:
//                System.out.println("----Enter the Details----");
//
//                System.out.print("orderId: ");
//                int orderId = -1;
//                while (true) {
//                    String oidStr = sc.nextLine();
//                    try {
//                        orderId = Integer.parseInt(oidStr);
//                        break;
//                    } catch (NumberFormatException e) {
//                        System.out.print("Invalid orderId, please enter a number: ");
//                    }
//                }
//
//                System.out.print("customerName: ");
//                String custName = sc.nextLine();
//
//                System.out.print("Status (default Pending): ");
//                String status = sc.nextLine();
//                if (status.isEmpty()) {
//                    status = "Pending";
//                }
//
//                Order newOrder = new Order(orderId, new Date(System.currentTimeMillis()), custName, status);
//                createOrder(newOrder);
//                break;
//
//            case 2:
//                readOrders();
//                break;
//
//            case 3:
//                System.out.print("Order ID: ");
//                int id = -1;
//                while (true) {
//                    String idStr = sc.nextLine();
//                    try {
//                        id = Integer.parseInt(idStr);
//                        break;
//                    } catch (NumberFormatException e) {
//                        System.out.print("Invalid Order ID, please enter a number: ");
//                    }
//                }
//
//                System.out.print("New Status: ");
//                String newStatus = sc.nextLine();
//                updateOrderStatus(id, newStatus);
//                break;
//
//            case 4:
//                System.out.print("Order ID to delete: ");
//                int delId = -1;
//                while (true) {
//                    String delStr = sc.nextLine();
//                    try {
//                        delId = Integer.parseInt(delStr);
//                        break;
//                    } catch (NumberFormatException e) {
//                        System.out.print("Invalid Order ID, please enter a number: ");
//                    }
//                }
//                deleteOrder(delId);
//                break;
//
//            case 5:
//                System.out.println("Exiting...");
//                break;
//        }
        switch (choice) {
            case 1:
                System.out.println("----Enter the Details----");
//                System.out.println("orderId: ");
//                int orderId = sc.nextInt();

                System.out.println("customerName: ");
                String custName = sc.next();

//                System.out.println("Status: ");  // make changes here
                String status_default = "Pending";

                Order newOrder = new Order(new Date(System.currentTimeMillis()), custName, status_default);
                createOrderWithItems(newOrder);
                break;
            case 2:
                readOrders();
                break;
            case 3:
                System.out.print("Order ID: ");
                int id = sc.nextInt();
                sc.nextLine();
                System.out.println("Select New Status:");
                System.out.println("1-Pending | 2-Completed ");
                int st = sc.nextInt();
                sc.nextLine();
                updateOrderStatusChoice(id, st);
                break;
            case 4:
                System.out.print("Order ID to delete: ");
                int delId = sc.nextInt();
                deleteOrder(delId);
                break;
        }

    }
}
