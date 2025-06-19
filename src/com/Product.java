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

    public Product(String name, String description, double price, int stockQuantity, int supplierId) {
//        this.productId = productId;
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
            stmt.setInt(5, product.getSupplierId());

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

    public static Product getProductById(int productId) {
        String sql = "SELECT * FROM Product WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("supplier_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get current stock for a product
    public static int getProductStock(Connection conn, int productId) throws SQLException {
        String sql = "SELECT stock_quantity FROM Product WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        }
        throw new SQLException("Product not found: " + productId);
    }

    // Update product stock
    public static synchronized void updateProductStock(Connection conn, int productId, int newStock) throws SQLException {
        String sql = "UPDATE Product SET stock_quantity = ? WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    // New method to sell product, handles stock decrement and can be called by concurrent threads
    public synchronized void sellProduct(Connection conn, int quantityToSell) throws SQLException { // ADDED METHOD
        int currentStock = getProductStock(conn, this.productId);
        if (currentStock < quantityToSell) {
            System.out.println("Not enough stock for product " + this.name + ". Current: " + currentStock + ", Requested: " + quantityToSell);
            throw new SQLException("Out of Stock for product ID: " + this.productId);
        }
        int newStock = currentStock - quantityToSell;
        updateProductStock(conn, this.productId, newStock);
        this.setStockQuantity(newStock); // Update the object's stockQuantity
        System.out.println("Product " + this.name + " stock updated to: " + newStock);
    }

    // Ensure product exists with initial stock
    public static void ensureProductExists(int productId, int initialStock) {
        String sql = "INSERT INTO Product (product_id, name, description, price, stock_quantity, supplier_id) " +
                "VALUES (?, 'Test Product', 'Test Description', 9.99, ?, 1) " +
                "ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, initialStock);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to ensure product existence: " + e.getMessage());
        }
    }

    //Checking low stock
    public static void checkLowStock(int threshold) {
        String sql = "SELECT * FROM Product WHERE stock_quantity < ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n⚠️ Low Stock Products (Below " + threshold + " units):");
            System.out.printf("%-10s %-20s %-10s%n", "Product ID", "Name", "Stock");
            System.out.println("-------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10d %-20s %-10d%n",
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getInt("stock_quantity"));
            }

            if (!found) {
                System.out.println("✅ All products have sufficient stock.");
            }

        } catch (SQLException e) {
            System.err.println("Error checking low stock: " + e.getMessage());
        }
    }

    // ------------- Optional: main() to test ---------------
    public static void runProduct() {
        Scanner sc = new Scanner(System.in);
//        Console console = System.console();
        System.out.println("Choose action: 1-Create | 2-Read | 3-Update Price | 4-Delete | 5-Check for Low Stock");
        int choice = sc.nextInt();
//        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.println("----Enter the Details----");
//                System.out.println("productId: ");
//                int productId = sc.nextInt();
//                sc.nextLine();

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

                Product p = new Product(name_prod, description, price, stockQuantity, supplierId);
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
            case 5:
                System.out.print("Enter stock threshold: ");
                System.out.println("Stock less than 5: ");
//                int threshold = sc.nextInt();
//                checkLowStock(threshold);
                checkLowStock(5);

                break;
        }
    }
}
