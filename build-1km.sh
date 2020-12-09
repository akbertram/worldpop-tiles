#!/bin/sh

YEAR=2020

wget -q ftp://ftp.worldpop.org/GIS/Population/Global_2000_2020/${YEAR}/0_Mosaicked/ppp_${YEAR}_1km_Aggregated.tif

gdalwarp -t_srs EPSG:3857 ppp_${YEAR}_1km_Aggregated.tif ppp_${YEAR}_1km_Aggregated_mercator.tif

gdaldem color-relief ppp_${YEAR}_1km_Aggregated_mercator.tif colors.cpt ppp_${YEAR}_1km_Aggregated_colors.tif

gdal2tiles.py --profile=mercator -z 0-10 ppp_${YEAR}_1km_Aggregated_colors.tif tiles/${YEAR}

