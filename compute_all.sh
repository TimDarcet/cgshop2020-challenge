#!/bin/bash

FOLDER=$1
FOLDER="./clouds/challenge_instances/data"

for f in $(find $FOLDER -name '*.json')
do
    make TARGET=$f
done