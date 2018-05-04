#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
EC2_Name="${Stack_Name}-csye6225-ec2"

echo "Provided Stack name is: ${Stack_Name}"

export instanceId=$(aws ec2 describe-instances --query 'Reservations[*].Instances[*].[InstanceId,State.Name, Tags[*][?Value==`${EC2_Name}`]]' --output text|grep running|awk '{print $1}')

echo "Instance id is: ${instanceId}"

aws ec2 stop-instances --instance-ids $instanceId

aws ec2 wait instance-stopped --instance-ids $instanceId

echo "Instance is stopped!!"

aws ec2 modify-instance-attribute --no-disable-api-termination --instance-id $instanceId 

echo "Termination protection is removed from the instance!!"

aws cloudformation delete-stack --stack-name $Stack_Name

aws cloudformation wait stack-delete-complete --stack-name $Stack_Name

if [ $? -eq 0 ]; then

    echo "Stack deleted successfully!!"

else

    echo "Unable to delete stack. Please check the logs!!"

fi