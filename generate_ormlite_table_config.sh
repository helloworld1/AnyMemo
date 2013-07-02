#!/bin/sh

javac -cp 'build-tools/:bin/classes:libs/*' build-tools/DatabaseConfigUtil.java
java -cp 'build-tools/:libs/*:bin/classes/' DatabaseConfigUtil
rm build-tools/DatabaseConfigUtil.class
