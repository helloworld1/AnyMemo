#!/bin/sh
trap exit ERR
./gradlew assembleDevDebug
adb install -r -t -d app/build/outputs/apk/dev/debug/AnyMemo-dev-debug.apk
adb shell am start -n org.liberty.android.fantastischmemodev/org.liberty.android.fantastischmemo.ui.AnyMemo
