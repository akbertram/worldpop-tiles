#!/bin/bash

YEAR=2020
COUNTRIES=`cat countries.txt`

mkdir -p tif

for COUNTRY in $COUNTRIES
do
    if [ ! -f $TILE_DIR/${COUNTRY}_ppp_${YEAR}.tif ]; then
        echo "Downloading ${COUNTRY^^}"
        wget -q ftp://ftp.worldpop.org/GIS/Population/Global_2000_2020/${YEAR}/${COUNTRY^^}/${COUNTRY}_ppp_${YEAR}.tif -P $TILE_DIR
    fi
done
