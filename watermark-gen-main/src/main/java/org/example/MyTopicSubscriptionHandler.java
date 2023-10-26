package org.example;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.*;

import java.util.HashMap;
import java.util.Map;

public class MyTopicSubscriptionHandler implements RequestHandler<Map<String, Object>, String> {

    private static final String TABLE_NAME = "MySubscriptionTable";
    private static final String SUBSCRIPTION_STATUS_CONFIRMED = "confirmed";
    private static final String SUBSCRIPTION_STATUS_PENDING = "pending";

    private static final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build(); // Create DynamoDB client once
    private static final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient); // Create DynamoDB instance once

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        String email = (String) input.get("email"); // Email address provided as input

        String topicArn = "arn:aws:sns:us-east-1:520835858968:watermark-sns";


        Map<String, String> subscriptions = getAllTopicSubscriptions(topicArn);


        String subscriptionArn = subscribeToTopic(email, topicArn);
        // Check if the user is already subscribed

        if (subscriptionArn != null) {
            return "User is already subscribed with subscription ARN: " + subscriptionArn;
        }


               System.out.println("subscription sent to eamil:"+email);
        return "Subscription email sent to: " + email;
    }


    private String subscribeToTopic(String email, String topicArn) {
        AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
        SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, "email", email);
        SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
        return subscribeResult.getSubscriptionArn();
    }

    private Map<String, String> getAllTopicSubscriptions(String topicArn) {
        AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
        ListSubscriptionsByTopicRequest listSubscriptionsRequest = new ListSubscriptionsByTopicRequest().withTopicArn(topicArn);
        ListSubscriptionsByTopicResult listSubscriptionsResult = snsClient.listSubscriptionsByTopic(listSubscriptionsRequest);

        Map<String, String> subscriptionMap = new HashMap<>();
        for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
            String email = subscription.getEndpoint();
            String status = subscription.getSubscriptionArn().contains(":pending") ? "pending" : "confirmed";
            subscriptionMap.put(email, status);
        }

        return subscriptionMap;
    }
}
