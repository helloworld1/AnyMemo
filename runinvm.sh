#!/bin/bash

adb shell am start -e debug true -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n org.liberty.android.fantastischmemo/org.liberty.android.fantastischmemo.AnyMemo
