#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
VPC_Name="${Stack_Name}-csye6225-vpc"
InternetGateway_Name="${Stack_Name}-csye6225-InternetGateway"
PublicRouteTable_Name="${Stack_Name}-csye6225-public-route-table"
PrivateRouteTable_Name="${Stack_Name}-csye6225-private-route-table"
WebServerSubnet_Name="${Stack_Name}-csye6225-web-server-subnet"
DBServerSubnet_Name="${Stack_Name}-csye6225-db-server-subnet"

echo "Provided Stack name is: ${Stack_Name}"
echo "Desired VPC name is: ${VPC_Name}"
echo "Desired Internet Gateway name is: $InternetGateway_Name"
echo "Desired Public Route Table name is: $PublicRouteTable_Name"
echo "Desired Private Route Table name is: $PrivateRouteTable_Name"
echo "Desired Web Server Subnet name is: $WebServerSubnet_Name"
echo "Desired DB Server Subnet name is: $DBServerSubnet_Name"

echo "Starting creation of the stack!!"

aws cloudformation create-stack --stack-name $Stack_Name --template-body file://csye6225-cf-networking.json --parameters ParameterKey=VPCName,ParameterValue=$VPC_Name ParameterKey=InternetGatewayName,ParameterValue=$InternetGateway_Name ParameterKey=PublicRouteTableName,ParameterValue=$PublicRouteTable_Name ParameterKey=PrivateRouteTableName,ParameterValue=$PrivateRouteTable_Name ParameterKey=WebServerSubnetName,ParameterValue=$WebServerSubnet_Name ParameterKey=DBServerSubnetName,ParameterValue=$DBServerSubnet_Name

aws cloudformation wait stack-create-complete --stack-name $Stack_Name

if [ $? -eq 0 ]; then

    echo "Stack created successfully!!"

else

    echo "Unable to create stack. Please check the cloudformation logs!!"

fi