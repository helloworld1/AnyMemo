#!/bin/bash

adb shell am start -e debug true -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n org.liberty.android.fantastischmemo/org.liberty.android.fantastischmemo.AnyMemo
debug_port=$(adb jdwp | tail -1);
cmd="adb forward tcp:8700 jdwp:$debug_port"
echo $cmd
exec $cmd
echo 'DONE!'
