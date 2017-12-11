#!/bin/bash

#TODO: use sed -i 's/\r//' "$nms/$file" again
#TODO: check if folder names end with "net" or search for an alternative

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
	echo "Please run this script again with the clean decompile sources as an argument. In most cases this will be ../work/decompile-XXXX"
	exit
fi

# all starting with "net"
origDir=$1
modifiedDir=$2
patchDir=$3

fileCount=$(find $patchDir -type f | wc -l)
currentFileNo="0"
echo "$fileCount files total"

cp -n -R $origDir $(dirname $modifiedDir)

for file in $(find $patchDir -type f -not -path '*/\.*') ; do
	file=${file#$patchDir/}
	currentFileNo=$(($currentFileNo + 1))
	progress=$(($currentFileNo * 100 / $fileCount))
	echo -en "\r$progress%, "
	filename=$(basename "$file")
	extension="${filename##*.}"
	if [ "$extension" != "patch" ] ; then
		outName=$modifiedDir/$file
		patchedFile=$(cat "$patchDir/$file")
	else
		outName=${file%.*}.java
		patch --quiet -o /tmp/patched_mc_code.java $origDir/$outName $patchDir/$file
		patchedFile=$(cat /tmp/patched_mc_code.java) # why can't patch just output on stdout???
		rm /tmp/patched_mc_code.java
		outName="$modifiedDir/$outName"
	fi
	echo -en "Patching $outName\033[K"
	if [ -f "$outName" ] ; then

		patchedOld=$(cat "$outName")
		if [ "$patchedFile" != "$patchedOld" ] ; then
			echo -e "\n$outName changed"
			mkdir -p $(dirname ${outName})
			echo "$patchedFile" > "$outName"
		fi
	else
		if [[ $patchedFile = *[![:space:]]* ]] ; then
			echo -e "\nNew file $outName"
			mkdir -p $(dirname ${outName})
			echo "$patchedFile" > "$outName"
		fi
	fi
done

echo -en "\r\033[K"
