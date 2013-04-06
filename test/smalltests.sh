#!/bin/sh
trap exit ERR

adb shell am instrument -w -e size small org.liberty.android.fantastischmemo.test/android.test.InstrumentationTestRunner
