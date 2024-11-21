#!/bin/bash
aws apigateway get-rest-apis | jq -r '.items[] | [.id] | @tsv'