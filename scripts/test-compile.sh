#!/bin/sh
trap exit ERR
./gradlew installFreeDebug
./gradlew installFreeDebugTest
