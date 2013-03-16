#!/bin/sh

BASE="../../../.."
SYSDIR="$BASE/sys"

echo "sys : $SYSDIR"
echo "base : $BASE"

#
# Generate proof obligation files

rm "bfs.decl.p_*.p"

#echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" >> "$report"
#echo "<testsuite name=\"$1\" timestamp=\"$DATE\">" >> "$report"

java -cp "$BASE/ivil.jar" \
   -Dpseudo.log=0 \
   -Dpseudo.sysDir="$SYSDIR" \
   -Dpseudo.baseDir="$BASEDIR" \
   de.uka.iti.pseudo.justify.RuleJustification \
   -d "." ../bfs.decl.p
   

#
# Try to load proofs

for po in bfs.decl.p_*.p
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
      else
         echo "ERROR reloading proof for $po"
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
      else
         echo "ERROR auto proving $po"
      fi
   fi
done
