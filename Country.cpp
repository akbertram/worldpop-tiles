
#include <boost/timer/timer.hpp>

#include "Country.h"
#include "Gradient.h"
#include "TilePng.h"

using namespace std;


void Country::render() {
    cerr << "Starting " << path.filename() << endl;
    readImage();
    project();
    renderTiles();
    CPLFree(pSourceBuffer);
}


void Country::readImage() {

    boost::timer::cpu_timer timer;

    pSourceBuffer = (float *) CPLMalloc(sizeof(float)*(sourceWidth * sourceHeight));
    CPLErr error = pBand->RasterIO(GF_Read,
                                   0, 0, sourceWidth, sourceHeight, /* source rectangle */
                                   pSourceBuffer,
                                   sourceWidth,
                                   sourceHeight,
                                   GDT_Float32,
                                   sizeof(float), /* nPixelSpace */
                                   sizeof(float) * sourceWidth, /* nLineSpace (scanline) */
                                   nullptr);

    if(error != CE_None) {
        fprintf(stderr, "RasterIO returned %d\n", error);
        exit(-1);
    }

    cerr << "    Image read fully in " << timer.format(3);
}

void Country::project() {

    boost::timer::cpu_timer timer;

    this->projectionX.reserve(targetWidth);
    this->projectionY.reserve(targetHeight);

    double meterX = tiling.meterTileLeft(leftTile);
    for (int i = 0; i < targetWidth; i++) {
        double longitude = Tiling::metersToLongitude(meterX);
        projectionX.push_back(longitudeToPixel(longitude));
        meterX += tiling.GetMetersPerPixel();
    }

    double meterY = tiling.meterTileTop(topTile);
    for (int i = 0; i < targetHeight; i++) {
        double latitude = Tiling::metersToLatitude(meterY);
        projectionY.push_back(latitudeToPixel(latitude));
        meterY -= tiling.GetMetersPerPixel();
    }

    cerr << "   Projection complete in " << timer.format();
}


void Country::renderTiles() {

    boost::timer::cpu_timer timer;

    for (int tileX = 0; tileX < tileCountX; tileX++) {
        for (int tileY = 0; tileY < tileCountY; tileY++) {
            renderTile(tileX, tileY);
        }
    }

    cerr << "    Tiles rendered in " << timer.format();
}

thread_local uint8_t pixelBuffer[Tiling::pixelsPerTile * Tiling::pixelsPerTile];

void Country::renderTile(int tileX, int tileY) {

    int pixelIndex = 0;
    bool empty = true;

    if(!TilePng::tryReadTile(tiling.GetZoomLevel(), leftTile + tileX, topTile + tileY, pixelBuffer)) {
        memset(pixelBuffer, Gradient::transparent, Tiling::pixelsPerTile * Tiling::pixelsPerTile);
    }

    int startX = tileX * tiling.GetPixelsPerTile();
    int startY = tileY * tiling.GetPixelsPerTile();

    for (int y = 0; y < tiling.GetPixelsPerTile(); y++) {
        int gridY = projectionY[startY + y];

        for (int x = 0; x < tiling.GetPixelsPerTile(); x++) {
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
        TilePng::writeTile(tiling.GetZoomLevel(), leftTile + tileX, topTile + tileY, pixelBuffer);
    }
}
