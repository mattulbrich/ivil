#!/bin/sh

theory=$1
shift

for po in $theory/*.p
do
  echo "Looking for proof in '$po'"
  if [ -r "$po"xml ]
  then
     ivil "$@" -proof "$po"xml "$po"
  else
     ivil "$@" "$po"
  fi
done
