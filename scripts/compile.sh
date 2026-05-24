#!/bin/sh
trap exit ERR
./gradlew assembleDevApi18Debug
adb install -r -t -d app/build/outputs/apk/devApi18/debug/AnyMemo-dev-api18-debug.apk
adb shell am start -n org.liberty.android.fantastischmemodev/org.liberty.android.fantastischmemo.ui.AnyMemo
