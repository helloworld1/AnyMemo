#!/bin/sh
trap exit ERR
ant debug install
adb shell am start -n org.liberty.android.fantastischmemo/org.liberty.android.fantastischmemo.ui.AnyMemo
