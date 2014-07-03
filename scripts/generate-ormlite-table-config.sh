#!/bin/sh

javac -cp 'build-tools/:build/intermediates/classes/free/debug:eclipse-deps/libs/*' build-tools/DatabaseConfigUtil.java
java -cp 'build-tools/:build/intermediates/classes/free/debug:eclipse-deps/libs/*' DatabaseConfigUtil
rm build-tools/DatabaseConfigUtil.class
