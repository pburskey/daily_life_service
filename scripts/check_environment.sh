#!/bin/bash
aVariable=$1

if [[ ! -v $aVariable ]]; then
    echo "$aVariable is not set"
    exit 1
elif [[ -z "$aVariable" ]]; then
    echo "$aVariable is set to the empty string"
    exit 2
fi

exit 0