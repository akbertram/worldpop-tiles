#!/bin/bash

YEAR=2020
COUNTRIES=`cat countries.txt`

mkdir -p tif

# First generate all the tile

for COUNTRY in $COUNTRIES
do
    echo ${COUNTRY^^}
    if [ ! -f tif/${COUNTRY}_ppp_${YEAR}.tif ]; then
        echo "...Downloading"
        wget -q ftp://ftp.worldpop.org/GIS/Population/Global_2000_2020/${YEAR}/${COUNTRY^^}/${COUNTRY}_ppp_${YEAR}.tif -P tif
    fi
    if [ ! -f tif/${COUNTRY}_ppp_${YEAR}_mercator.tif ]; then
        echo "...Reprojecting"
        gdalwarp -t_srs EPSG:3857 tif/${COUNTRY}_ppp_${YEAR}.tif tif/${COUNTRY}_ppp_${YEAR}_mercator.tif
    fi
    if [ ! -f tif/${COUNTRY}_ppp_${YEAR}_colors.tif ]; then
        echo "...Colorizing"
    	gdaldem color-relief tif/${COUNTRY}_ppp_${YEAR}_mercator.tif colors-100m.cpt tif/${COUNTRY}_ppp_${YEAR}_colors.tif
    fi
    echo "...Generating tiles"
    gdal2tiles.py --profile=mercator -z 9 tif/${COUNTRY}_ppp_${YEAR}_colors.tif country/${COUNTRY}/${YEAR}

done






