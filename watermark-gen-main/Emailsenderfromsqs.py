import json
import boto3

def lambda_handler(event, context):
    sqs_client = boto3.client('sqs')
    sns_client = boto3.client('sns')
    
    record = event['Records'][0]
    
    msg_body_str = record['body']
    msg_body = json.loads(msg_body_str)
    print("Msg Body:", msg_body)
    s3PreSignedUrl = msg_body.get('s3PreSignedUrl')
    email = msg_body.get('email')
    
    print("Email:", email)
    print("s3PreSignedUrl:", s3PreSignedUrl)
    
    if email and s3PreSignedUrl:
        sns_message = f"s3PreSignedUrl Information: {s3PreSignedUrl}"
        
        topic_arn = 'arn:aws:sns:us-east-1:520835858968:watermark-sns'
        
        response = sns_client.publish(
            TargetArn=topic_arn,
            Message=sns_message,
            MessageAttributes={
                'email': {
                    'DataType': 'String',
                    'StringValue': email
                }
            }
        )
        
        print(f"Message sent to {email}: {sns_message}")
    
    receipt_handle = record['receiptHandle']
    queue_url = 'https://sqs.us-east-1.amazonaws.com/520835858968/watermark-sqs'
    sqs_client.delete_message(QueueUrl=queue_url, ReceiptHandle=receipt_handle)