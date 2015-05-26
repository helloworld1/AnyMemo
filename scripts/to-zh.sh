#!/bin/bash
if [ -d "src/main/res/values-zh-rTW" ]
then
        rm -rf src/main/res/values-zh-rTW
fi
mkdir src/main/res/values-zh-rTW
iconv -f utf8 -t gb2312 src/main/res/values-zh-rCN/strings.xml | iconv -f gb2312 -t big5 | iconv -f big5 -t utf8 > src/main/res/values-zh-rTW/strings.xml 



