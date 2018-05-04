#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
CodeDeployEC2S3PolicyName="CodeDeploy-EC2-S3"
EC2CloudWatchPolicyName="EC2-CloudWatch-Policy"
TravisUploadToS3PolicyName="Travis-Upload-To-S3"
TravisCodeDeployPolicyName="Travis-Code-Deploy"
CodeDeployEC2ServiceRoleName="CodeDeployEC2ServiceRole"
CodeDeployServiceRoleName="CodeDeployServiceRole"
CodeDeployS3BucketName="xxxx"
TravisUser="Travis"
AWSRegion="us-east-1"
AWSAccountID="xxxx"
CodeDeployApplicationName="CodeDeployApplication"

echo "Provided Stack name is: ${Stack_Name}"

echo "Starting creation of the stack!!"

aws cloudformation create-stack --stack-name $Stack_Name --capabilities "CAPABILITY_NAMED_IAM" --template-body file://csye6225-cf-ci-cd.json --parameters ParameterKey=CodeDeployEC2S3PolicyName,ParameterValue=$CodeDeployEC2S3PolicyName ParameterKey=EC2CloudWatchPolicyName,ParameterValue=$EC2CloudWatchPolicyName ParameterKey=TravisUploadToS3PolicyName,ParameterValue=$TravisUploadToS3PolicyName ParameterKey=TravisCodeDeployPolicyName,ParameterValue=$TravisCodeDeployPolicyName ParameterKey=CodeDeployEC2ServiceRoleName,ParameterValue=$CodeDeployEC2ServiceRoleName ParameterKey=CodeDeployServiceRoleName,ParameterValue=$CodeDeployServiceRoleName ParameterKey=CodeDeployS3BucketName,ParameterValue=$CodeDeployS3BucketName ParameterKey=TravisUser,ParameterValue=$TravisUser ParameterKey=AWSRegion,ParameterValue=$AWSRegion ParameterKey=AWSAccountID,ParameterValue=$AWSAccountID ParameterKey=CodeDeployApplicationName,ParameterValue=$CodeDeployApplicationName

aws cloudformation wait stack-create-complete --stack-name $Stack_Name

if [ $? -eq 0 ]; then

    echo "Stack created successfully!!"

else

    echo "Unable to create stack. Please check the cloudformation logs!!"

fi
