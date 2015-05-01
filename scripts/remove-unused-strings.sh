#!/bin/bash

srcDir='src/main/java' 
resDir='src/main/res'
stringsFile='src/main/res/values/strings.xml'

deleteLine ()
{
        # sed -i is not compatibal with BSD and GNU
        # mktemp not compatibal
        
        files=`ls -R $resDir/values*/strings.xml`
        for file in $files
        do
                sed /$1/d $file > /tmp/tmp.file
                mv /tmp/tmp.file $file
        done
}

stringNames=`grep -o 'name=\".*\"\s*>' $stringsFile | sed 's/name=\"\(.*\)\"\s*>/\1/g'`

for name in $stringNames
do
        apperanceCount=`grep -r --include="*.java" --include="*.xml" --exclude="strings.xml" $name $srcDir $resDir | wc -l`
        if [ "$apperanceCount" -eq 0 ]
        then
                echo "deleting string: $name" 
                deleteLine $name
        fi
done
