#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
VPC_Name="${Stack_Name}-csye6225-vpc"
InternetGateway_Name="${Stack_Name}-csye6225-InternetGateway"
RouteTable_Name="${Stack_Name}-csye6225-public-route-table"

echo "Provided Stack name is: ${Stack_Name}"
echo "Desired VPC name is: ${VPC_Name}"
echo "Desired Internet Gateway name is: $InternetGateway_Name"
echo "Desired Route Table name is: $RouteTable_Name"

# Code for vpc creation and naming the vpc

export vpcId=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 --output text|grep VPC|awk '{print $7}')

if [ $? -eq 0 ]; then

    #export vpcId=$(aws ec2 describe-vpcs --query 'Vpcs[?IsDefault==`false`].VpcId' --output text)

    aws ec2 create-tags --resources $vpcId --tags Key=Name,Value=$VPC_Name

    echo "VPC has been created with name: ${VPC_Name}"

    # Code for creating internet gateway, naming the internet gateway and attaching internet gateway to above created vpc

    export internetGatewayId=$(aws ec2 create-internet-gateway --output text|grep INTERNETGATEWAY|awk '{print $2}')

    if [ $? -eq 0 ]; then

        #export internetGatewayId=$(aws ec2 describe-internet-gateways --query 'InternetGateways[*].[InternetGatewayId, Tags[0].Value]' --output text| grep None| awk '{print $1}')

        aws ec2 create-tags --resources $internetGatewayId --tags Key=Name,Value=$InternetGateway_Name

        aws ec2 attach-internet-gateway --internet-gateway-id $internetGatewayId --vpc-id $vpcId

        echo "Internet Gateway has been created with name ${InternetGateway_Name} and attached to vpc ${VPC_Name}"

        # Code for creating Route Table and creating route for 0.0.0.0/0 through above created Internet Gateway

        export routeTableId=$(aws ec2 create-route-table --vpc-id $vpcId --output text|grep ROUTETABLE|awk '{print $2}')

        if [ $? -eq 0 ]; then

            #export routeTableId=$(aws ec2 describe-route-tables --query 'RouteTables[*].[RouteTableId, VpcId, Associations[0].Main]' --output text| grep $vpcId | grep False | awk '{print $1}')

            aws ec2 create-tags --resources $routeTableId --tags Key=Name,Value=$RouteTable_Name

            aws ec2 create-route --route-table-id $routeTableId --destination-cidr-block 0.0.0.0/0 --gateway-id $internetGatewayId

            echo "Route Table has been created with name ${RouteTable_Name} and route to 0.0.0.0/0 has been created through Gateway ${InternetGateway_Name}"

        else

            echo "Unable to create route table. Check the configuration!!"
            exit 1

        fi

    else

        echo "Unable to create internet gateway. Check the configuration!!"
        exit 1

    fi

else

    echo "Unable to create vpc. Check the configuration!!"
    exit 1

fi