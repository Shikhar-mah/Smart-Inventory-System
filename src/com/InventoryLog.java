package com;

import com.utilities.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class InventoryLog {
    private int logId;
    private int productId;
    private int changeQuantity;
    private String action;
    private Timestamp timestamp;

    // Constructors
    public InventoryLog() {
    }

    public InventoryLog(int logId, int productId, int changeQuantity, String action, Timestamp timestamp) {
        this.logId = logId;
        this.productId = productId;
        this.changeQuantity = changeQuantity;
        this.action = action;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getChangeQuantity() {
        return changeQuantity;
    }

    public void setChangeQuantity(int changeQuantity) {
        this.changeQuantity = changeQuantity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // toString
    @Override
    public String toString() {
        return "InventoryLog{" +
                "logId=" + logId +
                ", productId=" + productId +
                ", changeQuantity=" + changeQuantity +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    // ---------- JDBC CRUD Methods ------------

    public static synchronized void createLog(InventoryLog log) {
        String sql = "INSERT INTO InventoryLog (product_id, change_quantity, action) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, log.getProductId());
            stmt.setInt(2, log.getChangeQuantity());
            stmt.setString(3, log.getAction());

            stmt.executeUpdate();
            System.out.println("Log inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readLogs() {
        String sql = "SELECT * FROM InventoryLog ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InventoryLog log = new InventoryLog(
                        rs.getInt("log_id"),
                        rs.getInt("product_id"),
                        rs.getInt("change_quantity"),
                        rs.getString("action"),
                        rs.getTimestamp("timestamp")
                );
                System.out.println(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteLog(int logId) {
        String sql = "DELETE FROM InventoryLog WHERE log_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, logId);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " row(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Optional main() for testing ----------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("1-Create | 2-Read | 3-Delete");
        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                InventoryLog log = new InventoryLog(0, 1, -5, "Shipped", null); // sample values
                createLog(log);
                break;
            case 2:
                readLogs();
                break;
            case 3:
                System.out.print("Enter Log ID to delete: ");
                int delId = sc.nextInt();
                deleteLog(delId);
                break;
        }
    }
}
