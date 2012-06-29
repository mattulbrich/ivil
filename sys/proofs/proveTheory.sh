#!/bin/sh

PROOFSDIR=`dirname "$0"`
SYSDIR="$PROOFSDIR/.."
BASE="$SYSDIR/.."
DATE=`date +%F-%T`

#echo "proofs : $PROOFSDIR"
#echo "sys : $SYSDIR"
#echo "base : $BASE"

if [ -z $1 ]
then
  echo "Needs argument!"
  exit 1
fi


#
# Generate proof obligation files

target="$PROOFSDIR/$1"
report="$target/report.xml"

mkdir -p "$target"
rm -f "$target"/*.p
rm -f "$report"

echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" >> "$report"
echo "<testsuite name=\"$1\" timestamp=\"$DATE\">" >> "$report"

java -cp "$BASE/ivil.jar" \
   -Dpseudo.log=0 \
   -Dpseudo.sysDir="$SYSDIR" \
   -Dpseudo.baseDir="$BASEDIR" \
   de.uka.iti.pseudo.justify.RuleJustification \
   -d "$target" \
   -include "../proveHelper.p" \
   "$SYSDIR/$1"

#
# Try to load proofs

for po in "$target"/*.p
do
   name=${po##*/$1_}
   name=${name%.p}

   echo "[Proving $name]"
   if [ -r "$po"xml ]
   then
      java -cp "$BASE/ivil.jar" \
        -Dpseudo.log=100 \
        -Dpseudo.sysDir="$SYSDIR" \
        -Dpseudo.baseDir="$BASEDIR" \
        de.uka.iti.pseudo.cmd.Main \
        -c -p "$po"xml "$po"
      if [ $? -eq 0 ]
      then
         echo "Reload proof for $po successful"
         echo "  <testcase classname=\"RuleJustification\" name=\"$name\" />" >> "$report"
      else
         echo "ERROR reloading proof for $po"
         echo "  <testcase classname=\"RuleJustification\" name=\"$name\" ><error message=\"Reload failed!\"/></testcase>" >> "$report"
      fi
   else
      echo "No proof for $po - try automatic"
      java -cp "$BASE/ivil.jar" \
        -Dpseudo.log=100 \
        -Dpseudo.sysDir="$SYSDIR" \
        -Dpseudo.baseDir="$BASEDIR" \
        de.uka.iti.pseudo.cmd.Main \
        "$po"
      if [ $? -eq 0 ]
      then
         echo "Auto proof for $po successful"
         echo "  <testcase classname=\"RuleJustification\" name=\"$name\" />" >> "$report"
      else
         echo "ERROR auto proving $po"
         echo "  <testcase classname=\"RuleJustification\" name=\"$name\" ><error message=\"Cannot autoprove!\"/></testcase>" >> "$report"
      fi
   fi
done
echo "</testsuite>" >> "$report"
