package com;

import com.utilities.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice;

    // Constructors
    public OrderItem() {
    }

    public OrderItem(int orderItemId, int orderId, int productId, int quantity, double unitPrice) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // toString
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }

    // ---------- JDBC CRUD Methods ------------

    public static void createOrderItem(OrderItem item) {
        String sql = "INSERT INTO OrderItem (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitPrice());

            stmt.executeUpdate();
            System.out.println("OrderItem inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readOrderItems() {
        String sql = "SELECT * FROM OrderItem";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OrderItem item = new OrderItem(
                        rs.getInt("order_item_id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                );
                System.out.println(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateQuantity(int orderItemId, int newQuantity) {
        String sql = "UPDATE OrderItem SET quantity = ? WHERE order_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, orderItemId);

            int rows = stmt.executeUpdate();
            System.out.println(rows + " row(s) updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrderItem(int orderItemId) {
        String sql = "DELETE FROM OrderItem WHERE order_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderItemId);

            int rows = stmt.executeUpdate();
            System.out.println(rows + " row(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Optional main() for testing ----------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("1-Create | 2-Read | 3-Update Quantity | 4-Delete");
        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                OrderItem item = new OrderItem(0, 1, 2, 3, 50.00); // sample values
                createOrderItem(item);
                break;
            case 2:
                readOrderItems();
                break;
            case 3:
                System.out.print("Enter OrderItem ID: ");
                int id = sc.nextInt();
                System.out.print("New Quantity: ");
                int qty = sc.nextInt();
                updateQuantity(id, qty);
                break;
            case 4:
                System.out.print("Enter OrderItem ID to delete: ");
                int delId = sc.nextInt();
                deleteOrderItem(delId);
                break;
        }
    }
}
