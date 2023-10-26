package org.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class watermarkgenerator implements RequestHandler<Map<String, Object>, String> {

    private static final String DESTINATION_BUCKET_NAME = "watermarkfileholder";

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        String base64PDF = (String) input.get("pdf"); // Base64-encoded PDF file
        String watermarkText = (String) input.get("watermark"); // Watermark string
        String recipientEmail = (String) input.get("email"); // Email address provided as input

        System.out.println("printing the watr amrk" + watermarkText + " and the recepent emaid" + recipientEmail);

        // Decode the Base64-encoded PDF to a byte array
        byte[] pdfBytes = Base64.getDecoder().decode(base64PDF);

        String topicArn = "arn:aws:sns:us-east-1:520835858968:watermark-sns";
        Map<String, String> subscriptions = getAllTopicSubscriptions(topicArn);

        // Checking for recipient's email already exists
        String subscriptionStatus = subscriptions.get(recipientEmail);
        System.out.println("subscription status of the user with" + recipientEmail + "and his satust is:" + subscriptionStatus);

        if (subscriptionStatus == null) {

            String message = "Please subscribe first to receive the watermarked PDF.";

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", message);

            String jsonResponse = convertToJson(responseData);

            return jsonResponse;
        }

        if (subscriptionStatus.equals("confirmed")) {

            System.out.println("printing it from if conditoin as status is confirmed");

            // Add watermark to the PDF
            byte[] pdfWithWatermark = addWatermarkToPDF(pdfBytes, watermarkText);

            // Upload the resulting PDF to S3 bucket
            String pdfFileName = "WaterMarked.pdf";
            String s3ObjectKey = uploadToS3(pdfWithWatermark, pdfFileName);

            // Get the S3 object URL
            String s3ObjectUrl = generateS3ObjectUrl(s3ObjectKey);

            String s3PreSignedUrl = generateS3PreSignedUrl(s3ObjectKey);

            System.out.println("printin the preassigned url of the user is:" + s3PreSignedUrl);
            System.out.println("printing before the sending the email");
            // Send email notification with the S3 object URL to the user email address
            //sendEmailNotification(s3PreSignedUrl, recipientEmail);
            sendToSQSQueue(s3PreSignedUrl, recipientEmail);

            // Create the response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Watermarked PDF created and uploaded successfully.");
            responseData.put("s3PreSignedUrl", s3PreSignedUrl);
            responseData.put("recipientEmail", recipientEmail);

            // Serialize the response data as JSON
            String jsonResponse = convertToJson(responseData);

            return jsonResponse;

        } else {
            //if the subscrition is still pending
            return "Please subscribe first to receive the watermarked PDF.";
        }
    }


    private String generateS3PreSignedUrl(String objectKey) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

        // Set the pre-signed URL expiration time for 1 hr
        long expirationTimeInMillis = 3600000; //

        // Generate the pre-signed URL
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(DESTINATION_BUCKET_NAME, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(new Date(System.currentTimeMillis() + expirationTimeInMillis));

        URL preSignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return preSignedUrl.toString();
    }

    private byte[] addWatermarkToPDF(byte[] pdfBytes, String watermarkText) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
            PdfContentByte content;

            int pageCount = reader.getNumberOfPages();
            for (int i = 1; i <= pageCount; i++) {
                content = stamper.getOverContent(i);
                content.beginText();

                PdfGState gState = new PdfGState();
                gState.setFillOpacity(0.3f);
                content.setGState(gState);

                content.setFontAndSize(baseFont, 30);
                content.showTextAligned(Paragraph.ALIGN_CENTER, watermarkText, 300, 400, 45);
                content.endText();
            }

            stamper.close();
            reader.close();

            return outputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error adding watermark to PDF: " + e.getMessage());
        }
    }

    // Method to get all subscriptions for the given topic and their statuses
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

    private String uploadToS3(byte[] data, String fileName) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        s3Client.putObject(DESTINATION_BUCKET_NAME, fileName, new ByteArrayInputStream(data), null);
        return fileName;
    }

    private String generateS3ObjectUrl(String objectKey) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        return s3Client.getUrl(DESTINATION_BUCKET_NAME, objectKey).toString();
    }

    private void sendToSQSQueue(String s3PreSignedUrl, String recipientEmail) {
        AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

        // Replace "YOUR_QUEUE_URL" with the URL of your SQS queue
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/520835858968/watermark-sqs";

        // Create a JSON object to hold the message data
        Map<String, String> messageData = new HashMap<>();
        messageData.put("email", recipientEmail);
        messageData.put("s3PreSignedUrl", s3PreSignedUrl);

        try {
            // Convert the message data to a JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String messageBody = objectMapper.writeValueAsString(messageData);

            // Send the message to the SQS queue
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(messageBody);
            sqsClient.sendMessage(sendMessageRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error sending message to SQS queue: " + e.getMessage());
        }
    }

    private String convertToJson(Map<String, Object> data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Error converting data to JSON: " + e.getMessage());
        }
    }


}