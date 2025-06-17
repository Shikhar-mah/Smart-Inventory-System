package com;

import com.utilities.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class Product {
    private int productId;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private int supplierId;

    // Default constructor
    public Product() {
    }

    // Parameterized constructor
    public Product(int productId, String name, String description, double price, int stockQuantity, int supplierId) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.supplierId = supplierId;
    }



    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    // toString
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", supplierId=" + supplierId +
                '}';
    }

    // ----------- JDBC CRUD METHODS BELOW -----------

    public static void createProduct(Product product) {
        String sql = "INSERT INTO Product (name, description, price, supplier_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setInt(4, product.getSupplierId());

            stmt.executeUpdate();
            System.out.println("Product created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void readProducts() {
        String sql = "SELECT * FROM Product";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("supplier_id")
                );
                System.out.println(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateProductPrice(int productId, double newPrice) {
        String sql = "UPDATE Product SET price = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newPrice);
            stmt.setInt(2, productId);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " product(s) updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteProduct(int productId) {
        String sql = "DELETE FROM Product WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            int rows = stmt.executeUpdate();
            System.out.println(rows + " product(s) deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------- Optional: main() to test ---------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Choose action: 1-Create | 2-Read | 3-Update Price | 4-Delete");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                Product p = new Product(0, "New Mouse", "Bluetooth mouse", 29.99, 9, 1);
                createProduct(p);
                break;
            case 2:
                readProducts();
                break;
            case 3:
                System.out.print("Enter product ID to update: ");
                int id = sc.nextInt();
                System.out.print("Enter new price: ");
                double newPrice = sc.nextDouble();
                updateProductPrice(id, newPrice);
                break;
            case 4:
                System.out.print("Enter product ID to delete: ");
                int deleteId = sc.nextInt();
                deleteProduct(deleteId);
                break;
        }
    }
}
