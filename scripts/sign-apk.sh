#!/bin/sh

flavor=$1
if [[ -z "$flavor" ]]; then
	echo "Usage: $0 flavor"
	echo "Flavor can be either free and pro."
	exit -1
fi
jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -keystore ~/Documents/keys/liberty-android-release.keystore "build/outputs/apk/AnyMemo-$flavor-release-unsigned.apk" liberty-android-key
zipalign -v 4 "build/outputs/apk/AnyMemo-$flavor-release-unsigned.apk" "build/outputs/apk/AnyMemo-$flavor-release.apk"
