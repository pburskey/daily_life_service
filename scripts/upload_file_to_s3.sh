#!/bin/bash

#stackName=$1
stackName=propertyconfiguration
#resoureceId=$2
resoureceId=PipelineStartBucket

payload=$1
payloadOnS3=$2


pipelineBucket=$(. describe_stack.sh $stackName $resoureceId)
pipelineBucket=$(echo "$pipelineBucket" | sed 's/"//g')
echo $pipelineBucket

aws s3 cp $payload s3://${pipelineBucket}/$payloadOnS3