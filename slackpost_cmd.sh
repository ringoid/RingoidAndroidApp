#!/usr/bin/env bash

# see https://gist.github.com/dopiaza/6449505
webhook_url="https://hooks.slack.com/services/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"
channel="#build-android"
username="Android Build"
message="$1 $2 $3"
./slackpost.sh $webhook_url $channel $username $message
