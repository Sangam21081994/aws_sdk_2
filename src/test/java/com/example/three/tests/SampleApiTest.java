package com.example.three.tests;

// SDK v2 Imports
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
// import com.amazonaws.auth.AWSCredentials; // Remove SDK v1
// import com.amazonaws.auth.BasicSessionCredentials; // Remove SDK v1
import com.example.three.base.BaseTest;
import com.example.three.services.MessagingService;
import com.example.three.utils.AwsConfigUtility;
import com.example.three.services.AwsCredentialService; 
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class SampleApiTest extends BaseTest {
    
    private MessagingService messagingService;
    
    @BeforeMethod 
    public void setupAwsService() throws IOException {
        // Load credentials using AwsCredentialService (now returns SDK v2 AwsCredentials)
        AwsCredentials credentials = AwsCredentialService.getGimmeAwsCredentials(); // Changed type to AwsCredentials
        
        String accessKey = null; // Initialize
        String secretKey = null; // Initialize
        String sessionToken = null; // Initialize to null

        // Check if the credentials are an instance of AwsSessionCredentials (SDK v2)
        if (credentials instanceof AwsSessionCredentials) {
            AwsSessionCredentials sessionCredentials = (AwsSessionCredentials) credentials;
            accessKey = sessionCredentials.accessKeyId(); // Use SDK v2 method
            secretKey = sessionCredentials.secretAccessKey(); // Use SDK v2 method
            sessionToken = sessionCredentials.sessionToken(); // Use SDK v2 method
        } else {
            // Handle case where it might be AwsBasicCredentials (no session token)
            // This assumes AwsCredentialService returns AwsBasicCredentials if no session token
            accessKey = credentials.accessKeyId(); // Use SDK v2 method
            secretKey = credentials.secretAccessKey(); // Use SDK v2 method
            System.err.println("Warning: AWS credentials did not include a session token or are not session credentials.");
        }

        // Initialize the AWS messaging service using the config utility and loaded credentials
        // Assuming MessagingService constructor was updated for SDK v2
        messagingService = new MessagingService(
            AwsConfigUtility.getSnsTopicArn(),
            AwsConfigUtility.getSqsQueueUrl(),
            AwsConfigUtility.getRegion(), // Use SDK v2 Region directly
            accessKey, 
            secretKey, 
            sessionToken
        );
        messagingService.initialize(); // Assuming initialize() is compatible or updated
        
        System.out.println("AWS messaging service initialized for thread: " + Thread.currentThread().getId());
    }
    
    @Test(description = "Test sending a message from JSON file to SNS and verifying in SQS")
    public void testSendMessageFromJsonFile() throws IOException, InterruptedException {
        System.out.println("Executing testSendMessageFromJsonFile on thread: " + Thread.currentThread().getId());
        
        // Send message from JSON file using the path from config utility
        String messageId = messagingService.sendMessageFromJsonFile(AwsConfigUtility.getMessageJsonPath());
        
        // Verify we got a message ID
        getSoftAssert().assertNotNull(messageId, "Message ID should not be null");
        
        // Check the message in SQS
        boolean messageVerified = messagingService.checkMessageInSqs(messageId, 5, 3, 2);
        
        // Assert the verification result
        getSoftAssert().assertTrue(messageVerified, "Message should be successfully verified in SQS");
    
        getSoftAssert().assertAll();
    }
}
