#!/bin/sh

jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -keystore ~/Documents/keys/liberty-android-release.keystore bin/AnyMemo-release-unsigned.apk liberty-android-key
zipalign -v 4 bin/AnyMemo-release-unsigned.apk bin/AnyMemo-release.apk
