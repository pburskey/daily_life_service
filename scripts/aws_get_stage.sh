#!/bin/bash

rest_id=$1

aws apigateway get-stages --rest-api-id $rest_id | jq -r '.item[] | [.stageName] | @tsv'