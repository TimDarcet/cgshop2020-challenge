#!/bin/bash

FOLDER=$1
FOLDER="../clouds/"

for f in $(find $FOLDER -name '*.json')
do
    make TARGET=$f
done