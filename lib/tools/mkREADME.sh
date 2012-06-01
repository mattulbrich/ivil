
# call this script in the main directory of ivil!

for d in `find -type d`
do
  if [ -r $d/README ]
  then
    # strip leading "./"
    echo "Directory ${d:2}:"
    # read up to first empty line
    perl -npe 'exit if /^ *$/;' $d/README
    echo
  fi
done
