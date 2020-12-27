#include <iostream>
#include "gdal_priv.h"
#include "cpl_conv.h" // for CPLMalloc()

#include "Country.h"
#include "Gradient.h"
#include "DownSampler.h"


int main() {
    GDALDataset  *poDataset;
    GDALAllRegister();

    int zoom = 11;

    Tiling tiling(zoom);
    Country country(tiling, "/home/alex/dev/worldpop-tiles/tif_country/bgd_ppp_2020.tif");
    country.renderTiles();
    zoom--;

    while(zoom >= 1) {
        int tileCount = Tiling::tileCountAtZoomLevel(zoom);
        for (int tileX = 0; tileX < tileCount; tileX++) {
            for (int tileY = 0; tileY < tileCount; tileY++) {
                DownSampler downSampler(zoom, tileX, tileY);
                downSampler.downSample();
            }
        }
        zoom--;
    }
    return 0;
}
