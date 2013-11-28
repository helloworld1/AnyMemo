#!/bin/sh

jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -keystore ~/Documents/keys/liberty-android-release.keystore build/apk/AnyMemo-release-unsigned.apk liberty-android-key
zipalign -v 4 build/apk/AnyMemo-release-unsigned.apk build/apk/AnyMemo-release.apk
