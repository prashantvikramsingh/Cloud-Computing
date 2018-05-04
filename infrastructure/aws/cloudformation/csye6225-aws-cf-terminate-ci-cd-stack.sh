#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1

echo "Provided Stack name is: ${Stack_Name}"

echo "Starting deletion of the stack!!"

aws cloudformation delete-stack --stack-name $Stack_Name

aws cloudformation wait stack-delete-complete --stack-name $Stack_Name

if [ $? -eq 0 ]; then

    echo "Stack deleted successfully!!"

else

    echo "Unable to delete stack. Please check the logs!!"

fi