package com.example.three.services;

// Add these SDK v2 imports if not already present
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Service for loading AWS credentials from gimme-aws-creds
 */
public class AwsCredentialService {
    
    /**
     * Loads AWS credentials (including session token) from the ~/.aws/credentials file (SDK v2)
     * 
     * @return AwsCredentials object (specifically AwsSessionCredentials if a session token is present)
     * @throws IOException if the credentials file cannot be read or required fields are missing
     */
    public static AwsCredentials getGimmeAwsCredentials() throws IOException { // Changed return type to AwsCredentials
        // Get the user's home directory
        String userHome = System.getProperty("user.home");
        File credentialsFile = new File(userHome + "/.aws/credentials");
        
        if (!credentialsFile.exists()) {
            throw new IOException("AWS credentials file not found at: " + credentialsFile.getAbsolutePath());
        }
        
        // Load the credentials from the file
        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFile))) {
            String line;
            boolean defaultProfileFound = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Look for the [default] profile section
                if (line.equals("[default]")) {
                    defaultProfileFound = true;
                    continue;
                }
                
                // If we're in the default profile section, parse the credentials
                if (defaultProfileFound && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    properties.setProperty(key, value);
                    
                    // If we've found a different profile section, stop parsing
                    if (line.startsWith("[") && line.endsWith("]")) {
                        break;
                    }
                }
            }
        }
        
        // Extract the access key, secret key, and session token
        String accessKey = properties.getProperty("aws_access_key_id");
        String secretKey = properties.getProperty("aws_secret_access_key");
        String sessionToken = properties.getProperty("aws_session_token"); // Get session token
        
        if (accessKey == null || secretKey == null) {
            throw new IOException("AWS access key or secret key not found in the credentials file");
        }
        
        System.out.println("AWS credentials loaded successfully");
        System.out.println("Access Key ID: " + maskString(accessKey));
        
        // If a session token is present, use AwsSessionCredentials (SDK v2)
        if (sessionToken != null && !sessionToken.isEmpty()) {
            System.out.println("Session Token: Present (masked)");
            return AwsSessionCredentials.create(accessKey, secretKey, sessionToken); // Changed to AwsSessionCredentials.create
        } else {
            // Fallback to AwsBasicCredentials if no session token (SDK v2)
            System.out.println("Session Token: Not found, using basic credentials");
            return AwsBasicCredentials.create(accessKey, secretKey); // Changed to AwsBasicCredentials.create
        }
    }
    
    /**
     * Masks a string for secure logging
     * 
     * @param input The string to mask
     * @return The masked string
     */
    private static String maskString(String input) {
        if (input == null || input.length() <= 4) {
            return "****";
        }
        
        // Show only the first 4 characters, mask the rest
        return input.substring(0, 4) + "****************";
    }
}