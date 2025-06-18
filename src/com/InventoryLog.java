package com;

import com.utilities.DBConnection;
import com.utilities.FileExport;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

//    public static void exportToFile(){
//        FileExport fex = new FileExport("InventoryLog.txt");
//
//    }

    public static void exportToFile() {
        String sql = "SELECT * FROM inventorylog";
        List<String> inventoryLogList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Timestamp timestamp = rs.getTimestamp("timestamp");
                String formattedTime = (timestamp != null)
                        ? timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "N/A";

                String row = String.format("%d | %d | %d | %s | %s",
                        rs.getInt("log_id"),
                        rs.getInt("product_id"),
                        rs.getInt("change_quantity"),
                        rs.getString("action"),
                        formattedTime);
                inventoryLogList.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching product data: " + e.getMessage());
        }

        FileExport fex = new FileExport(inventoryLogList ,"inventoryLog.txt");
    }



    // ---------- Optional main() for testing ----------
    public static void runInventoryLog() {
        Scanner sc = new Scanner(System.in);
        System.out.println("1-Read | 2-Delete | 3-Export to File");
        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                readLogs();
                break;
            case 2:
                System.out.print("Enter Log ID to delete: ");
                int delId = sc.nextInt();
                deleteLog(delId);
                break;
            case 3:
                exportToFile();
                break;
            default:
                System.out.println("Invalid Input... Try again.");

        }
    }
}
