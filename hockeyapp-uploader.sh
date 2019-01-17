#!/bin/sh

# locations of various tools
CURL=curl

SERVER_ENDPOINT=https://rink.hockeyapp.net/api/2

# Put your HockeyApp APP_TOKEN here. Find it in your HockeyApp account settings.
APP_TOKEN="eceebb5c4fd144429d86542ee9a30765"

# ipa - required, file data of the .ipa for iOS, .app.zip for OS X, or .apk file for Android
IPA=$1

# dsym - optional, file data of the .dSYM.zip file (iOS and OS X) or mapping.txt (Android);
# note that the extension has to be .dsym.zip (case-insensitive) for iOS and OS X and the file name has to be mapping.txt for Android.
DSYM=$2

# notes - optional, release notes as Textile or Markdown (after 5k characters note are truncated)
NOTES=$3

# notes_type - optional, type of release notes:
# 0 for Textile
# 1 for Markdown
NOTES_TYPE=""

# optional, notify testers (can only be set with full-access tokens):
# 0 to not notify testers
# 1 to notify all testers that can install this app
# 2 - Notify all testers
NOTIFY=""

# status - optional, download status (can only be set with full-access tokens):
# 1 to not allow users to download the version
# 2 to make the version available for download
STATUS="2"

# strategy - optional, replace or add build with same build number
# add to add the build as a new build to even if it has the same build number (default)
# replace to replace to a build with the same build number
STRATEGY=""

# tags - optional, restrict download to comma-separated list of tags
TAGS=""

# teams - optional, restrict download to comma-separated list of team IDs; example:
# 12,23,42 with 12, 23, and 42 being the database IDs of your teams
TEAMS=""

# users - optional, restrict download to comma-separated list of user IDs; example:
# 1224,5678 with 1224 and 5678 being the database IDs of your users
USERS=""

# mandatory - optional, set version as mandatory:
# 0 for not mandatory
# 1 for mandatory
MANDATORY=""

# release_type - optional, set the release type of the app:
# 2 for alpha
# 0 for beta [default]
# 1 for store
# 3 for enterprise
RELEASE_TYPE=""

# private - optional, set to true to enable the private download page (default is true)
PRIVATE=""

# owner_id - optional, set to the ID of your organization
OWNER_ID=""

# commit_sha - optional, set to the git commit sha for this build
COMMIT_SHA=$4

# build_server_url - optional, set to the URL of the build job on your build server
BUILD_SERVER_URL=""

# repository_url - optional, set to your source repository
REPOSITORY_URL=""

usage() {
	echo "Usage: hockeyapp-uploader.sh IPA"
	echo
}

verify_tools() {
	# Windows users: this script requires curl. If not installed please get from http://cygwin.com/

	# Check 'curl' tool
	"${CURL}" --help >/dev/null
	if [ $? -ne 0 ]; then
		echo "Could not run curl tool, please check settings"
		exit 1
	fi
}

verify_settings() {
	if [ -z "${APP_TOKEN}" ]; then
		usage
		echo "Please update APP_TOKEN with your private API key, as noted in the Settings page"
		exit 1
	fi
}

if [ $# -ne 1 ]; then
	usage
	exit 1
fi

# before even going on, make sure all tools work
verify_tools
verify_settings

if [ ! -f "${IPA}" ]; then
	usage
	echo "Can't find file: ${IPA}"
	exit 2
fi

#/bin/echo -n "Uploading ${IPA} to HockeyApp.. "
JSON=$( "${CURL}" \
        -s ${SERVER_ENDPOINT}/apps/upload \
        -H "X-HockeyAppToken: ${APP_TOKEN}" \
        -F "ipa=@${IPA}" \
        -F "dsym=@${DSYM}" \
        -F "notes=${NOTES}" \
        -F "notes_type=${NOTES_TYPE}" \
        -F "notify=${NOTIFY}" \
        -F "status=${STATUS}" \
        -F "strategy=${STRATEGY}" \
        -F "tags=${TAGS}" \
        -F "teams=${TEAMS}" \
        -F "users=${USERS}" \
        -F "mandatory=${MANDATORY}" \
        -F "release_type=${RELEASE_TYPE}" \
        -F "private=${PRIVATE}" \
        -F "owner_id=${OWNER_ID}" \
        -F "commit_sha=${COMMIT_SHA}" \
        -F "build_server_url=${BUILD_SERVER_URL}" \
        -F "repository_url=${REPOSITORY_URL}" \
)

URL=$( echo ${JSON} | sed 's/\\\//\//g' | sed -n 's/.*"public_url"\s*:\s*"\([^"]*\)".*/\1/p' )
if [ -z "$URL" ]; then
	echo "FAILED!"
	echo
	echo "Build uploaded, but no reply from server. Please contact support@hockeyapp.net"
	exit 1
fi

#echo "OK!"
#echo
#echo "Build was successfully uploaded to HockeyApp and is available at:"
echo ${URL}

