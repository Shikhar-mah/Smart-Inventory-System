package com;

import com.utilities.DBConnection;

import java.io.Console;
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
        String sql = "INSERT INTO Product (name, description, price, stock_quantity, supplier_id) VALUES (?, ?, ?, ?, ?)";
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
        System.out.println("Reading products: ");
        String sql = "SELECT * FROM Product";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-10s %-20s %-40s %-10s %-15s %-12s%n",
                    "Product ID", "Name", "Description", "Price", "Stock Qty", "Supplier ID");
            System.out.println("---------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("supplier_id")
                );

                // Truncate long descriptions to avoid breaking the table
                String desc = p.getDescription();
                if (desc.length() > 37) {
                    desc = desc.substring(0, 37) + "...";
                }

                System.out.printf("%-10d %-20s %-40s %-10.2f %-15d %-12d%n",
                        p.getProductId(),
                        p.getName(),
                        desc,
                        p.getPrice(),
                        p.getStockQuantity(),
                        p.getSupplierId()
                );
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
    public static void runProduct() {
        Scanner sc = new Scanner(System.in);
//        Console console = System.console();
        System.out.println("Choose action: 1-Create | 2-Read | 3-Update Price | 4-Delete");
        int choice = sc.nextInt();
//        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.println("----Enter the Details----");
                System.out.println("productId: ");
                int productId = sc.nextInt();
                sc.nextLine();

                System.out.println("name: ");
                String name_prod = sc.nextLine();

                System.out.println("Description: ");
                String description = sc.nextLine();

                System.out.println("price: ");
                double price = sc.nextDouble();
                sc.nextLine();

                System.out.println("stockQuantity: ");
                int stockQuantity = sc.nextInt();
                sc.nextLine();

                System.out.println("supplierId: ");
                int supplierId = sc.nextInt();
                sc.nextLine();

                Product p = new Product(productId, name_prod, description, price, stockQuantity, supplierId);
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
