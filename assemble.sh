#!/usr/bin/env bash
./gradlew clean
./gradlew assembleRelease
./sign_apk.sh $@

