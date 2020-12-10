#!/bin/sh

YEAR=2020

if [ ! -f tif/ppp_${YEAR}_1km_Aggregated.tif ]; then
    wget -q ftp://ftp.worldpop.org/GIS/Population/Global_2000_2020/${YEAR}/0_Mosaicked/ppp_${YEAR}_1km_Aggregated.tif -P tif
fi

if [ ! -f tif/ppp_${YEAR}_1km_Aggregated_mercator.tif ]; then
    gdalwarp -t_srs EPSG:3857 tif/ppp_${YEAR}_1km_Aggregated.tif tif/ppp_${YEAR}_1km_Aggregated_mercator.tif
fi

gdaldem color-relief tif/ppp_${YEAR}_1km_Aggregated_mercator.tif colors.cpt tif/ppp_${YEAR}_1km_Aggregated_colors.tif

gdal2tiles.py --profile=mercator -z 0-14 tif/ppp_${YEAR}_1km_Aggregated_colors.tif tiles/${YEAR}

