#!/bin/bash
scriptBase=$(pwd)
cd ..
mavenBase=$(pwd)
cd $scriptBase

environment_variable=aws_bucket
$(. $scriptBase/check_environment.sh $environment_variable)
if [ $? -ne 0 ]; then
  echo "Missing environment variable: $environment_variable"
  exit 1
fi

aws_bucket=$aws_bucket

environment_variable=aws_environment
$(. $scriptBase/check_environment.sh $environment_variable)
if [ $? -ne 0 ]; then
  echo "Missing environment variable: $environment_variable"
  exit 1
fi

aws_environment=$aws_environment
aws_environment=dev


resources_file=resources.txt
rm $mavenBase/src/test/resources/$resources_file

cd $mavenBase
mvn package

sam deploy \
--s3-bucket $aws_bucket
#--config-env $aws_environment
aws_return=$?
if [ $aws_return -ne 0 ] && [ $aws_return -ne 1 ]; then
  echo "Failed during aws deployment: $aws_return"
  exit 1
fi


#cd $scriptBase
#. aws_build_url_for_function.sh PartyFindByIDLambda / 2>&1 | tee -a $scriptBase/$resources_file
#mv $resources_file $mavenBase/src/test/resources
