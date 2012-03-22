#!/bin/sh
trap exit ERR
ant clean
ant debug install
