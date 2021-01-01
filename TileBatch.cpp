
#include <atomic>
#include <boost/timer/timer.hpp>

#include "TileBatch.h"
#include "Gradient.h"
#include "TilePng.h"

using namespace std;

atomic_long tileCount(0);
boost::timer::cpu_timer totalTimer;

void TileBatch::Run() {
    ReadImage();
    Project();
    RenderTiles();
    CPLFree(pSourceBuffer);
    tileCount += tileRect.GetTileCount();
    PrintStatistics();
}


void TileBatch::Project() {

    projectionX.reserve(targetWidth);
    projectionY.reserve(targetHeight);

    double meterX = tiling.MeterTileLeft(tileRect.GetLeftTile());
    for (int i = 0; i < targetWidth; i++) {
        double longitude = Tiling::MetersToLongitude(meterX);
        projectionX.push_back(round(LongitudeToPixel(longitude)));
        meterX += tiling.GetMetersPerPixel();
    }

    double meterY = tiling.MeterTileTop(tileRect.GetTopTile());
    for (int i = 0; i < targetHeight; i++) {
        double latitude = Tiling::MetersToLatitude(meterY);
        projectionY.push_back(round(LatitudeToPixel(latitude)));
        meterY -= tiling.GetMetersPerPixel();
    }
}

void TileBatch::ReadImage() {

    boost::timer::cpu_timer timer;

    GDALDataset *poDataset = (GDALDataset *) GDALOpen(country.GetPath().c_str(), GA_ReadOnly);
    GDALRasterBand *pBand = poDataset->GetRasterBand(1);

    size_t bufferSize = sizeof(int16_t) * (size_t)sourceWidth * (size_t)sourceHeight;
    pSourceBuffer = (int16_t*) CPLMalloc(bufferSize);
    if(pSourceBuffer == nullptr) {
        fprintf(stderr, "Could not allocate source buffer");
        exit(-1);
    }
    CPLErr error = pBand->RasterIO(GF_Read,
                                   sourceLeft, sourceTop, sourceWidth, sourceHeight,
                                   pSourceBuffer,
                                   sourceWidth,
                                   sourceHeight,
                                   GDT_Int16,
                                   0, /* nPixelSpace */
                                   0, /* nLineSpace (scanline) */
                                   nullptr);

    delete poDataset;

    if(error != CE_None) {
        fprintf(stderr, "RasterIO returned %d\n", error);
        exit(-1);
    }

    cerr << "    Image read fully in " << timer.format(3);
}

void TileBatch::RenderTiles() {

    boost::timer::cpu_timer timer;

    for (int tileX = 0; tileX < tileRect.GetTileCountX(); tileX++) {
        for (int tileY = 0; tileY < tileRect.GetTileCountY(); tileY++) {
            RenderTile(tileX, tileY);
        }
    }

    cerr << "    Tiles rendered in " << timer.format();
}

thread_local uint8_t pixelBuffer[Tiling::pixelsPerTile * Tiling::pixelsPerTile];

void TileBatch::RenderTile(int tileX, int tileY) {

    int pixelIndex = 0;
    bool empty = true;

    if(!TilePng::TryReadTile(tiling.GetZoomLevel(), tileRect.GetLeftTile() + tileX, tileRect.GetTopTile() + tileY,
                             pixelBuffer)) {
        memset(pixelBuffer, Gradient::transparent, Tiling::pixelsPerTile * Tiling::pixelsPerTile);
    }

    int startX = tileX * Tiling::pixelsPerTile;
    int startY = tileY * Tiling::pixelsPerTile;

    for (int y = 0; y < Tiling::pixelsPerTile; y++) {
        int gridY = projectionY[startY + y];

        for (int x = 0; x < Tiling::pixelsPerTile; x++) {
            int gridX = projectionX[startX + x];
            int pop = GetPopulation(gridX, gridY);

            if (pop >= 0) {
                pixelBuffer[pixelIndex] = Gradient::PopulationToColorIndex(pop);
                empty = false;
            }
            pixelIndex++;
        }
    }

    if(!empty) {
        TilePng::WriteTile(tiling.GetZoomLevel(), tileRect.GetLeftTile() + tileX, tileRect.GetTopTile() + tileY,
                           pixelBuffer);
    }
}

void TileBatch::PrintStatistics() {
    double secondsElapsed = totalTimer.elapsed().wall *  1e-9;
    double tilesRendered = tileCount;
    double tilesPerSecond = tilesRendered / secondsElapsed;

    cerr << "Rendered " << tilesPerSecond << " tiles per second." << endl;

}
