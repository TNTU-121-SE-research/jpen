#!/bin/bash

# Generates the providerNativeBuild.properties file
#
# Expected parameters when calling this script:
# 1: the directory where the svn revision is taken

getRevisionOnSvn(){
	# warning: you should call svn update before svn info
	revision=$(svn info "$1" | grep 'Last Changed Rev' | awk -F ':' '{print($2)}')
}

updateSvn(){
	echo updating svn src/main/c...
	# I use symlinks everywhere so it is safer to change to the target dir:
	originalDir=$(pwd)
	cd src/main/c
	svn up 
	cd $originalDir
}

getRevisionOnGit(){
	if [ ! -e "$1" ]
	then
		echo $1 does not exist
		exit 1
	fi
	revision=$(git log --format=oneline "$1" | wc -l)
	#sum 1000 to make it larger than the getRevisionOnSvn value
	revision=$(($revision+1000))
}

getRevision(){
	getRevisionOnGit "$1"
}

# $@: all the directories to combine
getCombinedRevision(){
	combinedRevision=0
	for i in "$@"
	do
		getRevision "$i"
		echo --- $i revision: $revision
		combinedRevision=$(($combinedRevision+$revision))
	done
}

propertyFile="nativeBuild.properties"
echo propertyFile=$propertyFile

echo \# DO NOT EDIT THIS FILE - it is generated by updateNativeBuildProperties.sh > $propertyFile
getCombinedRevision "src/main/c/linux" "src/main/c/utils"
echo jpen.provider.xinput.nativeBuild=$combinedRevision >> $propertyFile
getCombinedRevision "src/main/c/windows" "src/main/c/utils"
echo jpen.provider.wintab.nativeBuild=$combinedRevision >> $propertyFile
# nicarran: disable to set it manually because the osx native files are being provided by marcello:
# getCombinedRevision "src/main/c/osx"
combinedRevision=201
echo jpen.provider.osx.nativeBuild=$combinedRevision >> $propertyFile