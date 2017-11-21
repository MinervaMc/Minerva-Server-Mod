#!/bin/bash

if [ -z "$1" ]
then
    echo "Please run this script again with the clean decompile sources as an argument. In most cases this will be ../work/decompile-XXXX"
    exit
fi
cb=net/minecraft
nms="$1"


function traverse() {
for file in "$1"/*
do
    if [ -d "${file}" ] ; then
        echo "entering recursion with: ${file}"
        traverse "${file}"
    else
        echo "Diffing ${file}"
        #sed -i '' 's/\r//' "$nms/$file"
        #sed -i '' 's/\r//' "$file"
        outName=$(echo patches/"$(echo $file | cut -d. -f1)".patch)
        #echo "outName ${outName}"
        patchNew=$(diff -u --label net/minecraft/$file "$nms/$file" --label net/minecraft/$file "$file")
        if [ -f "$outName" ]
        then
            patchCut=$(echo "$patchNew" | tail -n +3)
            patchOld=$(cat "$outName" | tail -n +3)
            if [ "$patchCut" != "$patchOld" ] ; then
                echo "$outName changed"
                mkdir -p $(dirname ${outName})
                echo "$patchNew" > "$outName"
            fi
        else
            if [[ $patchNew = *[![:space:]]* ]] ; then
                echo "New patch $outName"
                mkdir -p $(dirname ${outName})
                echo "$patchNew" > "$outName"
            fi
        fi
    fi
done
}

traverse "$cb"
