#!/bin/sh
trap exit ERR
./gradlew installDebug
./gradlew installTest
