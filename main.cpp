#include <iostream>
#include "gdal_priv.h"
#include "cpl_conv.h" // for CPLMalloc()

#include "Country.h"
#include "Gradient.h"

void printcolor(unsigned int c) {
    uint8_t * bytes = (uint8_t*)&c;
    for(int i=0;i<4;++i) {
        printf("%02X ", bytes[i]);
    }
    printf("\n");
}

int main() {
    GDALDataset  *poDataset;
    GDALAllRegister();

    Tiling tiling(11);
    Country country(tiling, "/home/alex/dev/worldpop-tiles/tif_country/bgd_ppp_2020.tif");
    country.renderTiles();



    return 0;
}
