#!/bin/sh
trap exit ERR
./gradlew assembleDevApi16Debug
adb install -r app/build/outputs/apk/AnyMemo-free-debug.apk
adb shell am start -n org.liberty.android.fantastischmemo/org.liberty.android.fantastischmemo.ui.AnyMemo
