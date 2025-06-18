package com;

import com.utilities.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class Supplier {
    private int supplierId;
    private String name;
    private String contactEmail;
    private String phone;

    // Default constructor
    public Supplier() {
    }

    // Parameterized constructor
    public Supplier(int supplierId, String name, String contactEmail, String phone) {
        this.supplierId = supplierId;
        this.name = name;
        this.contactEmail = contactEmail;
        this.phone = phone;
    }

    // Getters and Setters
    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // toString method
    @Override
    public String toString() {
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", name='" + name + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    // ----------- JDBC CRUD OPERATIONS BELOW -----------

    public static void createSupplier(Supplier supplier) {
        String sql = "INSERT INTO Supplier (name, contact_email, phone) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactEmail());
            stmt.setString(3, supplier.getPhone());
            stmt.executeUpdate();
            System.out.println("Supplier created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readSuppliers() {
        String sql = "SELECT * FROM Supplier";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-12s %-20s %-30s %-15s%n", "Supplier ID", "Name", "Email", "Phone");
            System.out.println("----------------------------------------------------------------------------------------");

            while (rs.next()) {
                Supplier s = new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_email"),
                        rs.getString("phone")
                );

                // Print each row
                System.out.printf(
                        "%-12d %-20s %-30s %-15s%n",
                        s.getSupplierId(),
                        s.getName(),
                        s.getContactEmail(),
                        s.getPhone()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateSupplier(int id, String newName) {
        String sql = "UPDATE Supplier SET name = ? WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, id);
            int rowsAffected = stmt.executeUpdate();
            System.out.println(rowsAffected + " supplier(s) updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSupplier(int id) {
        String sql = "DELETE FROM Supplier WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println(rowsDeleted + " supplier(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------ Optional: main() for testing ------------
    public static void runSupplier() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Choose action: 1-Create | 2-Read | 3-Update | 4-Delete");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.println("----Enter the Details----");
                System.out.println("supplierId: ");
                int supplierId = sc.nextInt();

                System.out.println("name: ");
                String name = sc.next();

                System.out.println("contactEmail: ");
                String contactEmail = sc.next();

                System.out.println("phone: ");
                String phone = sc.next();

                Supplier s = new Supplier(supplierId, name, contactEmail, phone);
                createSupplier(s);
                break;
            case 2:
                readSuppliers();
                break;
            case 3:
                System.out.print("Enter supplier ID to update: ");
                int updateId = sc.nextInt();
                sc.nextLine();
                System.out.print("Enter new name: ");
                String newName = sc.nextLine();
                updateSupplier(updateId, newName);
                break;
            case 4:
                System.out.print("Enter supplier ID to delete: ");
                int deleteId = sc.nextInt();
                deleteSupplier(deleteId);
                break;
        }
    }
}
