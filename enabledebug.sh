#!/bin/sh

adb shell setprop debug.assert 1
adb shell setprop log.tag.StatementExecutor VERBOSE
adb shell setprop log.tag.BaseMappedStatement VERBOSE
adb shell setprop log.tag.MappedCreate VERBOSE


