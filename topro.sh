#!/bin/bash

cd ..
if [ -d "FantastischMemoPro" ]
then
	rm -rf FantastischMemoPro
fi
cp FantastischMemo FantastischMemoPro -r
cd FantastischMemoPro
mv src/org/liberty/android/fantastischmemo src/org/liberty/android/fantastischmemopro

find ./ -type f -name "*.java" | xargs sed -i 's/org\.liberty\.android\.fantastischmemo/org\.liberty\.android\.fantastischmemopro/g'
find ./ -type f -name "*.xml" | xargs sed -i 's/org\.liberty\.android\.fantastischmemo/org\.liberty\.android\.fantastischmemopro/g'
sed -i 's/Free Edition/Pro Edition/g' ./assets/about.html
sed -i 's/Fantastisch Memo\\nFree/Fantastisch Memo/g' ./res/values/strings.xml
sed -i 's/\"app_name\">Fantastisch Memo/\"app_name\">Fantastisch Memo Pro/g' ./res/values/strings.xml



