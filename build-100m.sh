#!/bin/bash

YEAR=2020
COUNTRIES="lby dza"

mkdir -p tif

# Download first...

for COUNTRY in $COUNTRIES
do
    if [ ! -f tif/${COUNTRY}_ppp_${YEAR}.tif ]; then
        echo "Downloading ${COUNTRY^^}"
        wget -q ftp://ftp.worldpop.org/GIS/Population/Global_2000_2020/${YEAR}/${COUNTRY^^}/${COUNTRY}_ppp_${YEAR}.tif -P tif
    fi
done



# Generate the tiles per country

for COUNTRY in $COUNTRIES
do
    echo ${COUNTRY^^}
    if [ ! -f tif/${COUNTRY}_ppp_${YEAR}_mercator.tif ]; then
        echo "...Reprojecting"
        gdalwarp -t_srs EPSG:3857 tif/${COUNTRY}_ppp_${YEAR}.tif tif/${COUNTRY}_ppp_${YEAR}_mercator.tif
    fi
    if [ ! -f tif/${COUNTRY}_ppp_${YEAR}_colors.tif ]; then
        echo "...Colorizing"
    	gdaldem color-relief -alpha tif/${COUNTRY}_ppp_${YEAR}_mercator.tif colors-100m.cpt tif/${COUNTRY}_ppp_${YEAR}_colors.tif
    fi
    echo "...Generating tiles"
    gdal2tiles.py --profile=mercator -z 9 tif/${COUNTRY}_ppp_${YEAR}_colors.tif country/${COUNTRY}/${YEAR}

done






