#!/usr/bin/env bash
function onMachine() {
	unameOut="$(uname -s)"
	case "${unameOut}" in
		Linux*)     machine=Linux;;
		Darwin*)    machine=Mac;;
		CYGWIN*)    machine=Cygwin;;
		MINGW*)     machine=MinGw;;
		*)          machine="UNKNOWN:${unameOut}"
	esac
	echo ${machine}
}

function isOnWindows() {
	if [ "$(onMachine)" = "MinGw" ]; then
		echo "true"
	else
		echo "false"
	fi
}

rm -rf ./release
mkdir ./release
STADPATH=${1:-~/Android/Sdk/build-tools/28.0.3}
PASSWORD=${3:1q@w3e4r5t}
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
	APKSIGNER=${STADPATH}/apksigner
	if [[ "$(isOnWindows)" == true ]]
	then
		APKSIGNER=${STADPATH}/apksigner.bat
		echo "${APKSIGNER} on Windows"
	else
		echo "${APKSIGNER} on Unix"
	fi
	if [[ -z $PASSWORD ]]
	then
		${APKSIGNER} sign --ks ${keystorePath} --out ./release/${filename}-aligned-signed.apk ./release/${filename}-aligned.apk
	else
		${APKSIGNER} sign --ks ${keystorePath} --ks-pass pass:${PASSWORD} --out ./release/${filename}-aligned-signed.apk ./release/${filename}-aligned.apk
	fi
	echo "ASSEMBLE: done ${filename}" ; ls -l ./release/${filename}-aligned-signed.apk
done
ls -l ./release
