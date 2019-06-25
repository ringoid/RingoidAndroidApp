#!/usr/bin/env bash
TO_SLACK=${1:-true}
if [[ "$TO_SLACK" == true ]]
then
	echo "Will post to Slack after deploy"
else
	echo "Deploy silently"
fi

function hockeyAppUpload() {
	./hockeyapp-uploader.sh $1
}

function slackPost() {
	./slackpost_cmd.sh $@
}

function getBuildCode() {
	echo "$1" | cut -d'-' -f 2
}

./gradlew clean
./gradlew assembleRelease
./sign_apk.sh $@
for FILE in `find ./release/ -name "*-aligned-signed.apk" -type f`
do
	cp $FILE ./old_releases
	filename=$(basename $FILE)
	echo "DEPLOY: ${filename}"
	url=$(hockeyAppUpload $FILE)
	echo "Build uploaded to URL: $url"
	buildVariant="*prod*"
	if [[ $filename =~ .*staging.* ]]
	then
		buildVariant="*staging*"
	fi
	buildCode=$(getBuildCode $filename)
#	if [[ "$TO_SLACK" == true ]]
#	then
		echo $(slackPost $buildVariant $buildCode $url)
		echo $(slackUpload $buildVariant $buildCode)
#	fi
done
