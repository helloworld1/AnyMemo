#!/bin/sh
trap exit ERR
cd ..
ant clean
cd test
ant clean
ant debug install
