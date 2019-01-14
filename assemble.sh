#!/usr/bin/env bash
./gradlew clean
./gradlew assembleProdRelease
./sign_apk.sh $@

