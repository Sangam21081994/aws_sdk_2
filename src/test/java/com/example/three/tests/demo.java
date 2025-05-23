package com.example.three.tests;

// SDK v2 Imports
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

// import com.amazonaws.auth.AWSCredentialsProvider; // Remove SDK v1
// import com.amazonaws.auth.DefaultAWSCredentialsProviderChain; // Remove SDK v1
// import com.amazonaws.regions.Regions; // Remove SDK v1
// import com.amazonaws.services.sns.AmazonSNS; // Remove SDK v1
// import com.amazonaws.services.sns.AmazonSNSClientBuilder; // Remove SDK v1
// import com.amazonaws.services.sns.model.MessageAttributeValue; // Remove SDK v1 - Name collision, be careful
// import com.amazonaws.services.sns.model.PublishRequest; // Remove SDK v1 - Name collision
// import com.amazonaws.services.sns.model.PublishResult; // Remove SDK v1 - Name collision

import java.util.HashMap;
import java.util.Map;

public class demo {

    // Replace with your SNS Topic ARN
    private static final String SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN"; 
    // Replace with your desired AWS Region (SDK v2 style)
    private static final Region AWS_REGION = Region.US_EAST_1; // Changed to SDK v2 Region

    public static void main(String[] args) {
        sendMessageToSns();
    }

    public static void sendMessageToSns() {
        // Hardcoded JSON payload
        String jsonPayload = "{\"type\":\"order\",\"source\":\"java-demo\",\"timestamp\":\"2023-10-27T12:00:00Z\",\"data\":{\"orderId\":78901,\"item\":\"widget\",\"quantity\":5,\"status\":\"pending\"}}";

        // Message attributes (SDK v2 style)
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("messageType", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("OrderNotification")
                .build());
        messageAttributes.put("priority", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("High")
                .build());
        messageAttributes.put("orderAmount", MessageAttributeValue.builder()
                .dataType("Number") // In SDK v2, Number type is directly supported for attributes
                .stringValue("150.75") 
                .build());

        try {
            // Use DefaultCredentialsProvider for SDK v2
            SnsClient snsClient = SnsClient.builder()
                    .region(AWS_REGION)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(SNS_TOPIC_ARN)
                    .message(jsonPayload)
                    .messageAttributes(messageAttributes)
                    // .subject("New Order Received - ID: 78901") // Optional: subject can be set here
                    .build();
            
            PublishResponse result = snsClient.publish(publishRequest); // Changed to PublishResponse

            System.out.println("Message sent successfully!");
            System.out.println("Message ID: " + result.messageId()); // Use messageId()
            if (result.sequenceNumber() != null) { // Use sequenceNumber()
                System.out.println("Sequence Number (for FIFO topics): " + result.sequenceNumber());
            }

        } catch (Exception e) {
            System.err.println("Error sending message to SNS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
