#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
VPC_Name="${Stack_Name}-csye6225-vpc"
InternetGateway_Name="${Stack_Name}-csye6225-InternetGateway"
RouteTable_Name="${Stack_Name}-csye6225-public-route-table"

export vpcId=$(aws ec2 describe-vpcs --query 'Vpcs[*].[VpcId, Tags[0].Value]' --output text|grep $VPC_Name|awk '{print $1}')

export internetGatewayId=$(aws ec2 describe-internet-gateways --query 'InternetGateways[*].[InternetGatewayId, Tags[0].Value]' --output text|grep $InternetGateway_Name|awk '{print $1}')

export routeTableId=$(aws ec2 describe-route-tables --query 'RouteTables[*].[RouteTableId,Tags[0].Value]' --output text| grep $RouteTable_Name|awk '{print $1}')

# Code for detaching Internet Gateway from its VPC and then deleting it

aws ec2 detach-internet-gateway --internet-gateway-id $internetGatewayId --vpc-id $vpcId

aws ec2 delete-internet-gateway --internet-gateway-id $internetGatewayId

if [ $? -eq 0 ]; then

    echo "Internet Gateway ${InternetGateway_Name} is detached from VPC ${VPC_Name} and has been deleted!!"

else

    echo "Unable to delete Internet Gateway ${InternetGateway_Name}. Please input correct Internet Gateway name!!"

fi

# Code for deleting secondary route table of the given VPC

aws ec2 delete-route-table --route-table-id $routeTableId

if [ $? -eq 0 ]; then

    echo "Route Table ${RouteTable_Name} has been deleted!!"

else

    echo "Unable to delete Route Table ${RouteTable_Name}. Please input correct Route Table name!!"

fi

# Code for deleting VPC and its primary route table

aws ec2 delete-vpc --vpc-id $vpcId

if [ $? -eq 0 ]; then

    echo "VPC ${VPC_Name} and its related components has been deleted!!"

else

    echo "Unable to delete VPC ${VPC_Name} and its related components. Please input correct VPC name!!"

fi