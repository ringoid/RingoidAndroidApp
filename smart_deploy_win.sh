#!/usr/bin/env bash
function strindex() {
    x="${1%%$2*}"
    [[ "$x" = "$1" ]] && echo -1 || echo "${#x}"
}

# find and increment build code
FILE="build.gradle"
TEMP="build.gradle.tmp"
BUILDSTR=$(grep "buildVersion = " $FILE)
echo "build string: $BUILDSTR"
INDEX=$(strindex "$BUILDSTR" "=")+2
CURRENT_VERSION=${BUILDSTR:$INDEX:3}
NEXT_VERSION=$((CURRENT_VERSION+1))
echo "current version: $CURRENT_VERSION"
echo "next version: $NEXT_VERSION"
sed "s/$CURRENT_VERSION/$NEXT_VERSION/g" "$FILE" > $TEMP && mv $TEMP "$FILE"

# commit
git diff
git add $FILE
git commit -m "Build $NEXT_VERSION (script)"
git show

# keep flavor
FLAVORSTR=$(grep "env = Environment" $FILE)
INDEX2=$(strindex "$FLAVORSTR" ".")+1
CURRENT_FLAVOR=${FLAVORSTR:$INDEX2}
echo "current flavor: $CURRENT_FLAVOR"

# deploy current flavor
echo "building $CURRENT_FLAVOR ..."
./deploy_win.sh

# change flavor
NEXT_FLAVOR="$CURRENT_FLAVOR"
if [[ $CURRENT_FLAVOR == "STAGING" ]]
then
    NEXT_FLAVOR="PRODUCTION"
else
    NEXT_FLAVOR="STAGING"
fi
echo "next flavor: $NEXT_FLAVOR"
NEXT_FLAVORSTR="${FLAVORSTR/$CURRENT_FLAVOR/$NEXT_FLAVOR}"
sed "s/$FLAVORSTR/$NEXT_FLAVORSTR/g" "$FILE" > $TEMP && mv $TEMP "$FILE"
git diff

# deploy new flavor
echo "building $NEXT_FLAVOR ..."
./deploy_win.sh

# set initial flavor
sed "s/$NEXT_FLAVORSTR/$FLAVORSTR/g" "$FILE" > $TEMP && mv $TEMP "$FILE"

# run Android Studio
# build and run apk
