#!/bin/bash

fn=$1
path=$2

#echo $fn

rest_id=$(sh aws_get_rest_api_id.sh)
#echo $rest_id


region=$(sh aws_get_region_for_function.sh $fn)
region=$(echo "$region" | sed 's/"//g')
#echo $region

stages=$(sh aws_get_stage.sh $rest_id)

for stage in $stages; do
  printf -v uri 'https://%s.execute-api.%s.amazonaws.com/%s%s' $rest_id $region $stage $path
  echo $uri
done




