package com;

import com.utilities.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class Order {
    private int orderId;
    private Date orderDate;
    private String customerName;
    private String status;

    // Constructors
    public Order() {
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

    public static void createOrder(Order order) {
        String sql = "INSERT INTO Orders (order_date, customer_name, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, order.getOrderDate());
            stmt.setString(2, order.getCustomerName());
            stmt.setString(3, order.getStatus());

            stmt.executeUpdate();
            System.out.println("Order created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readOrders() {
//        String sql = "SELECT * FROM Orders";
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//
//            System.out.printf("%-10s %-15s %-25s %-10s%n", "Order ID", "Order Date", "Customer Name", "Status");
//            System.out.println("---------------------------------------------------------------");
//
//            while (rs.next()) {
//                Order o = new Order(
//                        rs.getInt("order_id"),
//                        rs.getDate("order_date"),
//                        rs.getString("customer_name"),
//                        rs.getString("status")
//                );
//
//                System.out.printf(
//                        "%-10d %-15s %-25s %-10s%n",
//                        o.getOrderId(),
//                        o.getOrderDate().toString(), // or format if needed
//                        o.getCustomerName(),
//                        o.getStatus()
//                );
//            }
//
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
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
    public static void runOrder() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Choose: 1-Create | 2-Read | 3-Update Status | 4-Delete");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.println("----Enter the Details----");
                System.out.println("orderId: ");
                int orderId = sc.nextInt();

                System.out.println("customerName: ");
                String custName = sc.next();

                System.out.println("Status: ");  // make changes here
                String status_default = "Pending";

                Order newOrder = new Order(0, new Date(System.currentTimeMillis()), "John Doe", "Pending");
                createOrder(newOrder);
                break;
            case 2:
                readOrders();
                break;
            case 3:
                System.out.print("Order ID: ");
                int id = sc.nextInt(); sc.nextLine();
                System.out.print("New Status: ");
                String status = sc.nextLine();
                updateOrderStatus(id, status);
                break;
            case 4:
                System.out.print("Order ID to delete: ");
                int delId = sc.nextInt();
                deleteOrder(delId);
                break;
        }
    }
}
