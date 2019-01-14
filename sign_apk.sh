#!/usr/bin/env bash
rm -rf ./release
mkdir ./release
STADPATH=${1:-~/Android/Sdk/build-tools/28.0.3}
PASSWORD=${3:-}
echo "Standard path: ${STADPATH}"
for FILE in `find ./app/build/outputs/apk/ -name "*.apk" -type f`
do
	filename=$(basename $FILE)
	keystorePath=${2:-"./keystores/keystore_prod.jks"}
	if [[ $filename =~ .*staging.* ]]
	then
		keystorePath=${2:-"./keystores/keystore_stag.jks"}
	fi
	echo "ASSEMBLE: ${FILE} ${filename} :: keystore=${keystorePath}"
	filename="${filename%.*}"
	${STADPATH}/zipalign -v -p 4 ${FILE} ./release/${filename}-aligned.apk
	PASSWORD="q1W2e3r4t5"
	if [[ -z $PASSWORD ]]
	then
		${STADPATH}/apksigner sign --ks ${keystorePath} --out ./release/${filename}-aligned-signed.apk ./release/${filename}-aligned.apk
	else
		${STADPATH}/apksigner sign --ks ${keystorePath} --ks-pass pass:${PASSWORD} --out ./release/${filename}-aligned-signed.apk ./release/${filename}-aligned.apk
	fi
	echo "ASSEMBLE: done ${filename}" ; ls -l ./release/${filename}-aligned-signed.apk
done
ls -l ./release
