#!/bin/sh
trap exit ERR
./gradlew installFreeDebug
adb shell am start -n org.liberty.android.fantastischmemo/org.liberty.android.fantastischmemo.ui.AnyMemo
