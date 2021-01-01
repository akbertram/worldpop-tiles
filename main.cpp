#include <iostream>
#include <string>
#include <boost/filesystem.hpp>

#include "gdal_priv.h"

#include "Country.h"
#include "TileBatch.h"
#include "DownSampler.h"
#include "WorkerPool.h"

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
        cerr << "Starting " << entry.path() << endl;
        WorkerPool<TileBatch> workerPool;
        Country country(tiling, entry.path());
        for (TileRect &rect : country.DivideIntoBatches()) {
            TileBatch batch(country, rect);
            workerPool.Add(batch);
        }
        workerPool.Run();
    }

    zoom--;
    while(zoom >= 1) {
        cerr << "Down sampling zoom level "  << zoom << endl;
        WorkerPool<DownSampler> workerPool;
        int tileCount = Tiling::TileCountAtZoomLevel(zoom);
        for (int tileX = 0; tileX < tileCount; tileX++) {
            for (int tileY = 0; tileY < tileCount; tileY++) {
                DownSampler task = DownSampler(zoom, tileX, tileY);
                workerPool.Add(task);
            }
        }
        workerPool.Run();
        zoom--;
    }

    return 0;
}
