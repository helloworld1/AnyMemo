#!/bin/bash
if [ -d "res/values-zh-rTW" ]
then
        rm -rf res/values-zh-rTW
fi
mkdir res/values-zh-rTW
iconv -f utf8 -t gb2312 res/values-zh-rCN/strings.xml | iconv -f gb2312 -t big5 | iconv -f big5 -t utf8 > res/values-zh-rTW/strings.xml 



