#!/usr/bin/env bash
rm -rf ./app/build
rm -rf ./auth/build
rm -rf ./feed/build
rm -rf ./imagepreview/build
rm -rf ./main/build
rm -rf ./origin/build
rm -rf ./base/build
rm -rf ./domain/build
rm -rf ./data/build
rm -rf ./profile/build
rm -rf ./usersettings/build
rm -rf ./utility/build
rm -rf ./widget/build
./gradlew clean

