#!/bin/sh
trap exit ERR
adb shell am instrument -w -e class "org.liberty.android.fantastischmemo.test.$1" org.liberty.android.fantastischmemo.test/android.test.InstrumentationTestRunner
