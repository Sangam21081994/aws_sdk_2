Feature: AWS SNS and SQS Messaging
  As a developer
  I want to send messages to SNS topics
  So that they can be received by subscribed SQS queues

  Scenario: Send a message to SNS topic and verify it in SQS queue
    Given I have valid AWS credentials
    And I have configured SNS topic and SQS queue
    When I send a message from the JSON file to the SNS topic
    Then the message should be received in the SQS queue
    And I should be able to verify the message content