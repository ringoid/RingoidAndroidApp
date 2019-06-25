#!/usr/bin/env bash
# see https://api.slack.com/methods/files.upload
# see https://gist.github.com/polbins/0fa2902e6603245a2be6fc9a76a874c1
message="$1 $2"
for FILE in `find ./release/ -name "*-aligned-signed.apk" -type f`
do
    filename=$(basename $FILE)
    fullpath=$(readlink -f $FILE)
    ./slackupload.sh -f $fullpath -c '#build-android' -s 'xoxp-457467555377-520499192292-677354054327-74c455c51cfe9e92524500a0f6ca1d56' -x 'Please find .apk file here'
done
