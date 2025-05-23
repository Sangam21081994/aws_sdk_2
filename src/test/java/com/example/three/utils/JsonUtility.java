package com.example.three.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Utility class for reading and writing JSON files
 */
public class JsonUtility {

    /**
     * Reads a JSON file and returns its content as a JSONObject
     * 
     * @param filePath Path to the JSON file
     * @return JSONObject containing the file content
     * @throws IOException if the file cannot be read
     */
    public static JSONObject readJsonFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("JSON file not found at: " + filePath);
        }
        
        String content = new String(Files.readAllBytes(path));
        return new JSONObject(content);
    }
    
    /**
     * Writes a JSONObject to a file
     * 
     * @param jsonObject The JSONObject to write
     * @param filePath Path where the JSON file should be written
     * @throws IOException if the file cannot be written
     */
    public static void writeJsonFile(JSONObject jsonObject, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        // Create parent directories if they don't exist
        Files.createDirectories(path.getParent());
        
        // Write the JSON content to the file
        Files.write(path, jsonObject.toString(4).getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        System.out.println("JSON file written successfully to: " + filePath);
    }
    
    /**
     * Updates a JSON file with new values
     * 
     * @param filePath Path to the JSON file
     * @param updates JSONObject containing the updates to apply
     * @throws IOException if the file cannot be read or written
     */
    public static void updateJsonFile(String filePath, JSONObject updates) throws IOException {
        // Read the existing JSON file
        JSONObject existing = readJsonFile(filePath);
        
        // Apply the updates
        for (String key : updates.keySet()) {
            existing.put(key, updates.get(key));
        }
        
        // Write the updated JSON back to the file
        writeJsonFile(existing, filePath);
    }
}