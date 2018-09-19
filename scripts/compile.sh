#!/bin/sh
trap exit ERR
./gradlew assembleDevApi16Debug
adb install -r -t -d app/build/outputs/apk/devApi16/debug/AnyMemo-dev-api16-debug.apk
adb shell am start -n org.liberty.android.fantastischmemo/org.liberty.android.fantastischmemo.ui.AnyMemo
