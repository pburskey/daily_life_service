#!/bin/bash

stackName=$1
#stackName=propertyconfiguration
resoureceId=$2
#resoureceId=PipelineStartBucket

aws cloudformation describe-stack-resource \
--stack-name $stackName \
--logical-resource-id $resoureceId \
--query 'StackResourceDetail.PhysicalResourceId'