

#ifndef WORLDPOPTILES_TILEBATCH_H
#define WORLDPOPTILES_TILEBATCH_H


#include <gdal_priv.h>
#include "Tiling.h"
#include "Country.h"
#include "GeoRect.h"
#include "Task.h"

class TileBatch : public Task {

    Country country;
    Tiling tiling;
    TileRect tileRect;

    int sourceLeft;
    int sourceTop;
    int sourceWidth;
    int sourceHeight;

    int targetWidth;
    int targetHeight;

    std::vector<int> projectionX;
    std::vector<int> projectionY;

    int16_t *pSourceBuffer;


public:
    TileBatch(const Country& country,
              const TileRect &rect)
            : country(country), tiling(country.GetTiling()), tileRect(rect) {

        // Find the geographic bounds of this tile range (in degrees)
        GeoRect geoRect = tiling.TileRectToGeoRect(rect);

        // Now map these geographic bounds to a rectangle within the original country image (in pixels)
        sourceLeft = floor(country.longitudeToPixel(geoRect.GetLeft()));
        sourceTop = floor(country.latitudeToPixel(geoRect.GetTop()));
        int sourceRight = ceil(country.longitudeToPixel(geoRect.GetRight()));
        int sourceBottom = ceil(country.latitudeToPixel(geoRect.GetBottom()));

        // The source rectangle might lay outside the bounds of the image
        // So adjust...

        if(sourceLeft < 0) {
            sourceLeft = 0;
        }
        if(sourceRight > country.GetWidth()) {
            sourceRight = country.GetWidth();
        }
        if(sourceTop < 0) {
            sourceTop = 0;
        }
        if(sourceBottom > country.GetHeight()) {
            sourceBottom = country.GetHeight();
        }

        sourceWidth = sourceRight - sourceLeft;
        sourceHeight = sourceBottom - sourceTop;
        targetWidth = tileRect.GetTileCountX() * Tiling::pixelsPerTile;
        targetHeight = tileRect.GetTileCountY() * Tiling::pixelsPerTile;
    }

    void Run();

    static void PrintStatistics();

private:
    double LongitudeToPixel(double longitude) {
        return country.longitudeToPixel(longitude) - sourceLeft;
    }

    double LatitudeToPixel(double latitude) {
        return country.latitudeToPixel(latitude) - sourceTop;
    }

    inline int16_t GetPopulation(int x, int y) {
        if(x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight) {
            return -1;
        } else {
            float pop = pSourceBuffer[(y * sourceWidth) + x];
            return (int)pop;
        }
    }

    void Project();
    void ReadImage();
    void RenderTiles();
    void RenderTile(int tileX, int tileY);

};


#endif //WORLDPOPTILES_TILEBATCH_H
