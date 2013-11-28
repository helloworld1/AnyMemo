#!/bin/bash

cd ..
if [ -d "AnyMemoPro" ]
then
	rm -rf AnyMemoPro
fi
cp AnyMemo AnyMemoPro -r
cd AnyMemoPro
mv src/org/liberty/android/fantastischmemo src/org/liberty/android/fantastischmemopro

find ./ -type f -name "*.java" | xargs sed -i 's/org\.liberty\.android\.fantastischmemo/org\.liberty\.android\.fantastischmemopro/g'
find ./ -type f -name "*.xml" | xargs sed -i 's/org\.liberty\.android\.fantastischmemo/org\.liberty\.android\.fantastischmemopro/g'
sed -i 's/android:id="@+id\/misc_donate" android:visibility="visible"/android:id="@+id\/misc_donate" android:visibility="gone"/g' ./res/layout/misc_tab.xml
sed -i 's/\"app_name\">AnyMemo/\"app_name\">AnyMemo Pro/g' ./res/values/strings.xml
sed -i 's/\"app_full_name\">AnyMemo/\"app_full_name\">AnyMemo Pro/g' ./res/values/strings.xml
ant clean
./generate-ormlite-table-config.sh
