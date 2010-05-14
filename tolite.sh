#!/bin/bash

cd ..
if [ -d "AnyMemoLite" ]
then
	rm -rf AnyMemoLite
fi
cp AnyMemo AnyMemoLite -r
cd AnyMemoLite
mv src/org/liberty/android/fantastischmemo src/org/liberty/android/fantastischmemolite
rm -rf bin
rm -rf gen

find ./ -type f -name "*.java" | xargs sed -i 's/org\.liberty\.android\.fantastischmemo/org\.liberty\.android\.fantastischmemolite/g'
find ./ -type f -name "*.xml" | xargs sed -i 's/org\.liberty\.android\.fantastischmemo/org\.liberty\.android\.fantastischmemolite/g'
sed -i 's/AnyMemo Free/AnyMemo Lite/g' ./res/values/strings.xml
sed -i 's/\"app_name\">AnyMemo/\"app_name\">AnyMemo Lite/g' ./res/values/strings.xml
rm ./src/org/liberty/android/fantastischmemolite/TTS.java
sed -i '/questionTTS\./d' ./src/org/liberty/android/fantastischmemolite/MemoScreen.java
sed -i '/answerTTS\./d' ./src/org/liberty/android/fantastischmemolite/MemoScreen.java
sed -i '/new TTS/d' ./src/org/liberty/android/fantastischmemolite/MemoScreen.java
sed -i 's/TTS questionTTS/Object questionTTS = null/' ./src/org/liberty/android/fantastischmemolite/MemoScreen.java
sed -i 's/TTS answerTTS/Object answerTTS = null/' ./src/org/liberty/android/fantastischmemolite/MemoScreen.java
sed -i 's/minSdkVersion="4"/minSdkVersion="2"/' ./AndroidManifest.xml 
android update project --name AnyMemoLite --target 1 --path ./




