package com.example.three.utils;

// Replace SDK v1 import
// import com.amazonaws.regions.Regions;
// With SDK v2 import
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for AWS configuration values, loaded from a properties file.
 */
public class AwsConfigUtility {

    private static final Properties properties = new Properties();
    private static final String SNS_TOPIC_ARN;
    private static final String SQS_QUEUE_URL;
    private static final Region REGION; // Changed from Regions to Region
    private static final String MESSAGE_JSON_PATH;

    static {
        try (InputStream input = AwsConfigUtility.class.getClassLoader().getResourceAsStream("aws_config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find aws_config.properties");
                // Set default values or throw an error if the file is critical
                SNS_TOPIC_ARN = "YOUR_SNS_TOPIC_ARN_DEFAULT"; 
                SQS_QUEUE_URL = "YOUR_SQS_QUEUE_URL_DEFAULT";
                REGION = Region.US_EAST_1; // Changed from Regions.US_EAST_1 to Region.US_EAST_1
                MESSAGE_JSON_PATH = "src/test/resources/three/message_payload.json";
            } else {
                properties.load(input);
                SNS_TOPIC_ARN = properties.getProperty("sns.topic.arn");
                SQS_QUEUE_URL = properties.getProperty("sqs.queue.url");
                REGION = Region.of(properties.getProperty("aws.region")); // Changed from Regions.fromName to Region.of
                MESSAGE_JSON_PATH = properties.getProperty("message.json.path");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // Handle exception, perhaps by throwing a runtime exception or using defaults
            throw new RuntimeException("Failed to load aws_config.properties", ex);
        }
    }

    /**
     * Gets the SNS topic ARN
     * 
     * @return The SNS topic ARN
     */
    public static String getSnsTopicArn() {
        return SNS_TOPIC_ARN;
    }

    /**
     * Gets the SQS queue URL
     * 
     * @return The SQS queue URL
     */
    public static String getSqsQueueUrl() {
        return SQS_QUEUE_URL;
    }

    /**
     * Gets the AWS region
     * 
     * @return The AWS region
     */
    public static Region getRegion() { // Changed return type from Regions to Region
        return REGION;
    }

    /**
     * Gets the path to the message JSON file
     * 
     * @return The path to the message JSON file
     */
    public static String getMessageJsonPath() {
        return MESSAGE_JSON_PATH;
    }
}