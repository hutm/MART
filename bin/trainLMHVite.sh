#!/bin/sh

baseDir=$1

./bin/LNewMap -f WFC Chords $baseDir/empty.wmap

LMDIR=$baseDir/chordslm.0
LMDIR1=$baseDir/chordslm.1

mkdir $LMDIR
mkdir $LMDIR1
mkdir $baseDir/HViteData

./bin/LGPrep -T 1 -n 2 -s "Chords" -d $LMDIR -a 100000 -b 200000 $baseDir/empty.wmap $2

./bin/LGCopy -T 1 -o -b 200000 -m $LMDIR1/wmap -d $LMDIR1 -w $3 $LMDIR/wmap $LMDIR/gram.0
#TODO this line should be removed
./bin/LGCopy -T 1 -o -b 200000 -m $LMDIR1/wmap -d $LMDIR1 $LMDIR/wmap $LMDIR/gram.0

./bin/LBuild -T 1 -c 2 1 -n 2 $LMDIR1/wmap  $baseDir/HViteData/chords_htk2gr.lm $LMDIR1/data.0

./bin/HBuild -s "<s>" "</s>" -u "!!UNK" -z -n $baseDir/HViteData/chords_htk2gr.lm $3 $4