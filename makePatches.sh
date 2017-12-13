#!/bin/bash

#TODO: use sed -i 's/\r//' "$nms/$file" again
#TODO: check if folder names end with "net" or search for an alternative

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
	echo "Please run this script again with the clean decompiled sources, the modified decompiled sources and the patch folder as an argument. Example: ./makePatches.sh ../minecraft_server_orig/net ../minecraft_server_modified/net net"
	exit
fi

# all starting with "net"
origDir=$1
modifiedDir=$2
patchDir=$3

fileCount=$(find $modifiedDir -type f | wc -l)
currentFileNo="0"
echo "$fileCount files total"



for file in $(find $modifiedDir -type f -not -path '*/\.*') ; do
	file=${file#$modifiedDir/}
	currentFileNo=$(($currentFileNo + 1))
	progress=$(($currentFileNo * 100 / $fileCount))
	echo -en "\r$progress%, "


	if [ ! -f "$origDir/$file" ] ; then
		outName=$patchDir/$file
		patchNew=$(cat "$modifiedDir/$file")
	else
		outName=$patchDir/${file%.*}.patch
		patchNew=$(diff -u --label net/$file "$origDir/$file" --label net/$file "$modifiedDir/$file")



	fi
	echo -en "Diffing net/$file\033[K"
	if [ -f "$outName" ] ; then
		patchCut=$(echo "$patchNew" | tail -n +3)
		patchOld=$(cat "$outName" | tail -n +3)
		if [ "$patchCut" != "$patchOld" ] ; then
			echo -e "\n$outName changed"
			mkdir -p $(dirname ${outName})
			echo "$patchNew" > "$outName"
		fi
	else
		if [[ $patchNew = *[![:space:]]* ]] ; then
			echo -e "\nNew file $outName"
			mkdir -p $(dirname ${outName})
			echo "$patchNew" > "$outName"
		fi
	fi
done

echo -en "\r\033[K"
