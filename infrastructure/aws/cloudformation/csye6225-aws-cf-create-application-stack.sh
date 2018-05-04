#!/bin/bash

echo "Gathering Stack Name from the command line!!"

Stack_Name=$1
EC2ImageId="ami-66506c1c"
EC2InstanceType="t2.micro"
EC2VolumeSize="16"
EC2KeyName="csye6225"
DynamoDBTableName="csye6225"
BucketName="xxxx"
DBEngine="MySQL"
EngineVersion="5.6.37"
DBInstanceClass="db.t2.medium"
DBInstanceIdentifier="csye6225-spring2018"
MasterUsername="csye6225master"
MasterPassword="csye6225password"
DBName="csye6225"
SNSTopicName="resetpassword"
CodeDeployApplicationName="CodeDeployApplication"
CodeDeployGroupName="CodeDeployGroup"

echo "Provided Stack name is: ${Stack_Name}"

export vpcId=$(aws ec2 describe-vpcs --query "Vpcs[*].[CidrBlock,VpcId]" --output text|grep 10.0.0.0/16|awk '{print $2}')
export subnetId1=$(aws ec2 describe-subnets --query 'Subnets[*].[SubnetId, VpcId, AvailabilityZone]' --output text|grep $vpcId|grep us-east-1a|awk '{print $1}')
export subnetId2=$(aws ec2 describe-subnets --query 'Subnets[*].[SubnetId, VpcId, AvailabilityZone]' --output text|grep $vpcId|grep us-east-1b|awk '{print $1}')
export eC2SecurityGroupId=$(aws ec2 describe-security-groups --query 'SecurityGroups[*].[VpcId, Description, GroupId]' --output text|grep $vpcId|grep webapp|awk '{print $3}')
export rDSSecurityGroupId=$(aws ec2 describe-security-groups --query 'SecurityGroups[*].[VpcId, Description, GroupId]' --output text|grep $vpcId|grep rds|awk '{print $3}')
export loadBalancerSecurityGroupId=$(aws ec2 describe-security-groups --query 'SecurityGroups[*].[VpcId, Description, GroupId]' --output text|grep $vpcId|grep LoadBalancer|awk '{print $3}')
export eC2RoleName=$(aws iam list-roles --query 'Roles[*].[RoleName]' --output text|grep EC2Service|awk '{print $1}')
export codeDeployRoleName=$(aws iam list-roles --query 'Roles[*].[RoleName, Arn]' --output text|grep CodeDeployService| awk '{print $2}')
export HostedZoneId=$(aws route53 list-hosted-zones | grep Id | awk '{print$2}' | sed -e 's/^"//' -e 's/",//' | cut -d '/' -f 3)
export HostedZoneName=$(aws route53 list-hosted-zones --query "HostedZones[0].Name" --output text)
#export availabilityZone="[$(aws ec2 describe-availability-zones | grep ZoneName |awk '{print$2}'| tr '\n' ' '| cut -d ' ' -f 1,2,3,4,5,6| tr ' ' ',')]"

echo "Stack creation in progress!!"

aws cloudformation create-stack --stack-name $Stack_Name --capabilities "CAPABILITY_NAMED_IAM" --template-body file://csye6225-cf-application.json --parameters ParameterKey=EC2ImageId,ParameterValue=$EC2ImageId ParameterKey=EC2InstanceType,ParameterValue=$EC2InstanceType ParameterKey=EC2SecurityGroup,ParameterValue=$eC2SecurityGroupId ParameterKey=EC2VolumeSize,ParameterValue=$EC2VolumeSize ParameterKey=EC2KeyName,ParameterValue=$EC2KeyName ParameterKey=SubnetId1,ParameterValue=$subnetId1 ParameterKey=SubnetId2,ParameterValue=$subnetId2 ParameterKey=EC2RoleName,ParameterValue=$eC2RoleName ParameterKey=DynamoDBTableName,ParameterValue=$DynamoDBTableName ParameterKey=BucketName,ParameterValue=$BucketName ParameterKey=DBEngine,ParameterValue=$DBEngine ParameterKey=EngineVersion,ParameterValue=$EngineVersion ParameterKey=DBInstanceClass,ParameterValue=$DBInstanceClass ParameterKey=DBInstanceIdentifier,ParameterValue=$DBInstanceIdentifier ParameterKey=MasterUsername,ParameterValue=$MasterUsername ParameterKey=MasterPassword,ParameterValue=$MasterPassword ParameterKey=DBName,ParameterValue=$DBName ParameterKey=RDSSecurityGroup,ParameterValue=$rDSSecurityGroupId ParameterKey=LoadBalancerSecurityGroup,ParameterValue=$loadBalancerSecurityGroupId ParameterKey=SNSTopicName,ParameterValue=$SNSTopicName ParameterKey=VPCId,ParameterValue=$vpcId ParameterKey=HostedZoneId,ParameterValue=$HostedZoneId ParameterKey=HostedZoneName,ParameterValue=$HostedZoneName ParameterKey=CodeDeployRoleName,ParameterValue=$codeDeployRoleName ParameterKey=CodeDeployApplicationName,ParameterValue=$CodeDeployApplicationName ParameterKey=CodeDeployGroupName,ParameterValue=$CodeDeployGroupName

aws cloudformation wait stack-create-complete --stack-name $Stack_Name

if [ $? -eq 0 ]; then

    echo "Stack created successfully!!"

else

    echo "Unable to create stack. Please check the cloudformation logs!!"

fi
