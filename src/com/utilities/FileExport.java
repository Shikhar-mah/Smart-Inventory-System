// File: src/com/utilities/FileExport.java
package com.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class FileExport {

    static String filename;
    static List<String> data;

    public FileExport(List<String> data, String filename) {
        this.filename = filename;
        this.data = data;

        exportToTextFile();

    }


    // Method to export any list of strings to a text file
    public static void exportToTextFile(){  //List<String> data, String filename) {
        Scanner sc = new Scanner(System.in);

        // Ask user for a custom filename or use default
//        System.out.print("Enter filename to save (without extension) [" + defaultFilename + "]: ");
//        String filename;

//        if (filename.trim().isEmpty()) {
//            filename = defaultFilename; // Use default if empty
//        }

//        String fullFilename = filename.trim() + ".txt";

        // Write to file
        try (FileWriter writer = new FileWriter(filename)) {
            for (String line : data) {
                writer.write(line + "\n");
            }
            System.out.println("Data successfully exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}