#include <iostream>
#include <string>
#include <boost/filesystem.hpp>

#include "gdal_priv.h"

#include "Country.h"
#include "TileBatch.h"
#include "DownSampler.h"

using namespace boost::filesystem;
using namespace std;

int main(int argc, char *argv[]) {

    if(argc == 0) {
        cerr << "worldpoptiles [dir containing country tifs]" << endl;
        exit(-2);
    }

    GDALAllRegister();

    int zoom = 11;
    Tiling tiling(zoom);

    for (directory_entry& entry : directory_iterator(path(argv[1]))) {
        cout << "Starting " << entry.path() << '\n';
        Country country(tiling, entry.path());
        for (TileRect &rect : country.DivideIntoBatches()) {
            TileBatch batch(country, rect);
            batch.Render();
        }
    }

    TileBatch::PrintStatistics();

    zoom--;
    while(zoom >= 1) {
        cerr << "Down sampling zoom level "  << zoom << endl;
        int tileCount = Tiling::TileCountAtZoomLevel(zoom);
        for (int tileX = 0; tileX < tileCount; tileX++) {
            for (int tileY = 0; tileY < tileCount; tileY++) {
                DownSampler downSampler(zoom, tileX, tileY);
                downSampler.DownSample();
            }
        }
        zoom--;
    }

    return 0;
}
