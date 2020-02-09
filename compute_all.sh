#!/bin/bash

FOLDER=$1
FOLDER="./clouds/instances_01/challenge_instances/data"

for f in $(find $FOLDER -name '*.json')
do
    make TARGET=$f
done