#!/bin/sh
trap exit ERR

adb shell am instrument -w org.liberty.android.fantastischmemo.test/android.test.InstrumentationTestRunner
