aws lambda invoke \
--function-name PropertySaveLambda \
--invocation-type Event \
--cli-binary-format raw-in-base64-out \
--payload file://aws_invoke_payload.json \
response.json