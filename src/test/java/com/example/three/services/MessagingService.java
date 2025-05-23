package com.example.three.services;

// SDK v2 Imports
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Service class for AWS SNS/SQS messaging operations (AWS SDK v2)
 */
public class MessagingService {
    private final String snsTopicArn;
    private final String sqsQueueUrl;
    private final Region region; // Changed from Regions to Region
    private SnsClient snsClient; // Changed from AmazonSNS to SnsClient
    private SqsClient sqsClient; // Changed from AmazonSQS to SqsClient
    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String awsSessionToken;

    /**
     * Creates a new MessagingService with the specified AWS resource identifiers and session credentials
     *
     * @param snsTopicArn ARN of the SNS topic to publish to
     * @param sqsQueueUrl URL of the SQS queue to receive messages from
     * @param region AWS region where the resources are located (software.amazon.awssdk.regions.Region)
     * @param awsAccessKey AWS Access Key ID
     * @param awsSecretKey AWS Secret Access Key
     * @param awsSessionToken AWS Session Token
     */
    public MessagingService(String snsTopicArn, String sqsQueueUrl, Region region, // Changed parameter type
                            String awsAccessKey, String awsSecretKey, String awsSessionToken) {
        this.snsTopicArn = snsTopicArn;
        this.sqsQueueUrl = sqsQueueUrl;
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.awsSessionToken = awsSessionToken;
    }

    /**
     * Initializes the SNS and SQS clients with AWS credentials (AWS SDK v2)
     *
     * @throws IOException if the credentials cannot be loaded
     */
    public void initialize() throws IOException { // IOException might not be necessary if not loading from file
        AwsSessionCredentials credentials = AwsSessionCredentials.create(awsAccessKey, awsSecretKey, awsSessionToken);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // Initialize SNS client (SDK v2)
        this.snsClient = SnsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        // Initialize SQS client (SDK v2)
        this.sqsClient = SqsClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        System.out.println("AWS SDK v2 SNS and SQS clients initialized successfully");
    }

    /**
     * Sends a message from a JSON file to an SNS topic (AWS SDK v2)
     *
     * @param jsonFilePath Path to the JSON file containing the message payload
     * @return The application-specific message ID generated for tracking
     * @throws IOException if the JSON file cannot be read
     */
    public String sendMessageFromJsonFile(String jsonFilePath) throws IOException {
        // Read the JSON file
        String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

        // Add a unique ID for tracking
        JSONObject jsonObject = new JSONObject(jsonContent);
        String messageId = UUID.randomUUID().toString(); // Application-specific ID
        jsonObject.put("messageId", messageId);

        // Send the message to SNS (SDK v2)
        String jsonMessage = jsonObject.toString();
        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(snsTopicArn)
                .message(jsonMessage)
                .build();

        PublishResponse publishResult = snsClient.publish(publishRequest);

        System.out.println("Message sent to SNS with custom app ID: " + messageId);
        System.out.println("SNS Message ID: " + publishResult.messageId()); // SDK v2 uses messageId()

        return messageId; // Return your custom application-level messageId
    }

    /**
     * Checks if a message with a specific application ID is present in the SQS queue (AWS SDK v2)
     *
     * @param appMessageId The application-specific message ID to look for
     * @param waitTimeSeconds Time to wait for message propagation before first check
     * @param maxAttempts Maximum number of attempts to check the queue
     * @param delayBetweenAttempts Delay in milliseconds between check attempts
     * @return true if the message is found and deleted, false otherwise
     * @throws InterruptedException if the thread is interrupted while sleeping
     */
    public boolean checkMessageInSqs(String appMessageId, int waitTimeSeconds, int maxAttempts, int delayBetweenAttempts)
            throws InterruptedException {
        // Wait for the message to propagate to SQS
        System.out.println("Waiting " + waitTimeSeconds + " seconds for message to propagate to SQS...");
        Thread.sleep(waitTimeSeconds * 1000L);

        boolean messageFound = false;

        // Try multiple times to find the message, with delays between attempts
        for (int attempt = 0; attempt < maxAttempts && !messageFound; attempt++) {
            System.out.println("Checking SQS queue, attempt " + (attempt + 1) + " of " + maxAttempts);

            // Create a request to receive messages from the SQS queue (SDK v2)
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(sqsQueueUrl)
                    .maxNumberOfMessages(10)  // Receive up to 10 messages at once
                    .waitTimeSeconds(5)       // Wait up to 5 seconds for messages (long polling)
                    .build();

            // Receive messages from the SQS queue (SDK v2)
            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
            System.out.println("Received " + messages.size() + " messages");

            // Process each received message
            for (Message message : messages) {
                // Parse the message body as JSON
                String messageBody = message.body();

                // The message from SNS is wrapped in another JSON object
                // We need to extract the actual message from the "Message" field
                JSONObject snsWrapper = new JSONObject(messageBody);
                String actualMessageContent = snsWrapper.getString("Message"); // SNS wraps the original message

                // Parse the actual message as JSON
                JSONObject messageJson = new JSONObject(actualMessageContent);

                // Check if this is the message we're looking for using the application-specific ID
                if (appMessageId.equals(messageJson.optString("messageId"))) { // Use optString for safety
                    System.out.println("Found message with custom app ID: " + appMessageId);
                    System.out.println("Message content: " + messageJson.toString());

                    // Delete the message from the SQS queue (SDK v2)
                    DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                            .queueUrl(sqsQueueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build();
                    sqsClient.deleteMessage(deleteRequest);
                    System.out.println("Message deleted from SQS.");

                    messageFound = true;
                    break; // Exit the inner loop once the message is found
                }
            }

            if (!messageFound && attempt < maxAttempts - 1) {
                System.out.println("Message not found, waiting " + delayBetweenAttempts + " seconds before next attempt...");
                Thread.sleep(delayBetweenAttempts * 1000);
            }
        }
        
        if (messageFound) {
            System.out.println("Message verification successful");
        } else {
            System.out.println("Message verification failed after " + maxAttempts + " attempts");
        }
        
        return messageFound;
    }
}