#!/bin/sh

PROOFSDIR=`dirname "$0"`
SYSDIR="$PROOFSDIR/.."
BASE="$SYSDIR/.."

#echo "proofs : $PROOFSDIR"
#echo "sys : $SYSDIR"
#echo "base : $BASE"

#
# Generate proof obligation files

mkdir -p "$PROOFSDIR/$1"
java -cp "$BASE/ivil.jar" \
   -Dpseudo.log=0 \
   -Dpseudo.sysDir="$SYSDIR" \
   -Dpseudo.baseDir="$BASEDIR" \
   de.uka.iti.pseudo.justify.RuleJustification \
   -d "$PROOFSDIR/$1" \
   -include "../proveHelper.p" \
   "$SYSDIR/$1"

#
# Try to load proofs

for po in "$PROOFSDIR/$1"/*.p
do
   if [ -r "$po"xml ]
   then
      java -cp "$BASE/ivil.jar" \
        -Dpseudo.log=100 \
        -Dpseudo.sysDir="$SYSDIR" \
        -Dpseudo.baseDir="$BASEDIR" \
        de.uka.iti.pseudo.cmd.Main \
        -c -p "$po"xml "$po"
   else
      echo "No proof for $po"
   fi
done
