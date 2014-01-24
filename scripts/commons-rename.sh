#!/bin/bash


find ./ -type f -name "*.java" | xargs sed -i 's/org\.apache\.commons/org\.apache\.mycommons/g'


