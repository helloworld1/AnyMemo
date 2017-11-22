AnyMemo Readme
==============

What is AnyMemo
---------------

AnyMemo is a free open-sourced spaced repetition flashcard learning software similar to SuperMemo for Android mobile phones.
It implements an advanced adaptive scheduling algorithm based on modified Mnemosyne algorithm (Enhanced SuperMemo SM2 algorithm) to maximize the learning efficient.

AnyMemo will help you learn various languages like Arabic, Chinese, English, German, Spanish, French, Japanese, Italian, Korean, Esperanto.
Also you can learn histories, computer related topics, religion, life styles using AnyMemo too!

See detailed info at http://anymemo.org

Directory structure
-------------------

* app: The app source
* gradle: The gradle wrapper
* scripts: The scripts that automate some tasks

How to compile
--------------

AnyMemo is built using gradle. You need to make sure the following are installed in
order to build:
* Android SDK tools: http://developer.android.com/sdk/index.html
* JDK: http://www.oracle.com/technetwork/java/javase/downloads/index.html

In Android SDK tools' "Android SDK Manager", you need the following items:
* Android 8.0 SDK platform
* Android SDK tools
* Andoird SDK Build-tools version 24
* Android support repository
* Android support library

Once the dependency is satisfied, you need to set the ANDROID_HOME environment variable
to the Android SDK installation location. E. g.
```
export ANDROID_HOME=~/android-sdk-linux/
```


cd to the AnyMemo directory and Use gradle to compile the project

Move AMSecrets.java.template in `src/org/liberty/android/fantastischmemo/common/` to AMSecrets.
Provide the values to the fields in AMSecrets.java.

Them compile using gradle:
```
cd AnyMemo
./gradlew assembleFreeApi16Debug
```
To build pro version, use
```
./gradlew assembleProApi16Debug
```
AnyMemo has a dev flavor that changes the min API level to 21 to make the build much faster.
It is recommended for development only.
```
./gradlew assembleDevApi21Debug
```

The build artifacts are in
```
build/apk/
```

Instrumentation tests
---------------------
There are hundreds of intrumentation tests that ensure the basic funciton of
AnyMemo. To run these tests against free version, you need to run:
```
./gradlew installFreeApi16DebugTest
./gradlew connectedInstrumentTestFreeApi16Debug
```

To run against pro version:
```
./gradlew installProApi16DebugTest
./gradlew connectedInstrumentTestProApi16Debug
```


Eclipse users
-------------

Sorry, the support of Eclipse ended since Google does not support it well.

Android studio user
-------------------

It is easy! Install all the dependencies listed in the "How to compile" section
and you can import directly into Android studio.

