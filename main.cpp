#include <iostream>
#include <string>
#include "gdal_priv.h"
#include "cpl_conv.h" // for CPLMalloc()

#include "Country.h"
#include "CountryList.h"
#include "Gradient.h"
#include "DownSampler.h"

using namespace std;


int main(int argc, char *argv[]) {
    GDALDataset  *poDataset;
    GDALAllRegister();

    int zoom = 11;

    Tiling tiling(zoom);
    CountryList countryList(tiling, argv[1]);
    for(Country &country : countryList.countries) {
        country.render();
    }
    zoom--;

    while(zoom >= 1) {
        cerr << "Down sampling zoom level "  << zoom << endl;
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
