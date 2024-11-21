#!/bin/bash
fn=$1
aws lambda get-function-configuration --function-name $fn | jq '.FunctionArn | split(":")[3]'