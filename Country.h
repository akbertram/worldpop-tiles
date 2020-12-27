

#ifndef WORLDPOPTILES_COUNTRY_H
#define WORLDPOPTILES_COUNTRY_H

#include <png.h>
#include <boost/filesystem.hpp>
#include "gdal_priv.h"
#include "Tiling.h"

class Country {
    boost::filesystem::path path;
    GDALDataset *poDataset;
    bool ready;
    Tiling tiling;
    double longitudePerPixel;
    double latitudePerPixel;
    double topNorth;
    double bottomSouth;
    double leftWest;
    double rightEast;

    int topTile;
    int bottomTile;
    int leftTile;
    int rightTile;
    int tileCountX;
    int tileCountY;

    int sourceWidth;
    int sourceHeight;

    int targetWidth;
    int targetHeight;

    GDALRasterBand *pBand;
    float *pSourceBuffer;

    std::vector<int> projectionX;
    std::vector<int> projectionY;

public:
    Country(Tiling &rTiling, boost::filesystem::path path) : path(path), tiling(rTiling) {
        poDataset = (GDALDataset *) GDALOpen( path.c_str(), GA_ReadOnly );
        if(poDataset == nullptr) {
            return;
        }
        double adfGeoTransform[6];
        poDataset->GetGeoTransform(adfGeoTransform);
        longitudePerPixel = adfGeoTransform[1];
        latitudePerPixel = adfGeoTransform[5];

        topNorth = adfGeoTransform[3];
        bottomSouth = topNorth + (poDataset->GetRasterYSize() * latitudePerPixel);

        leftWest = adfGeoTransform[0];
        rightEast = leftWest + (poDataset->GetRasterXSize() * longitudePerPixel);

//        printf("North: %f\nSouth: %f\nWest: %f\nEast: %f\n", topNorth, bottomSouth, leftWest, rightEast);

        topTile = tiling.metersToTileY(Tiling::latitudeToMeters(topNorth));
        bottomTile = tiling.metersToTileY(Tiling::latitudeToMeters(bottomSouth));
        leftTile = tiling.metersToTileX(Tiling::longitudeToMeters(leftWest));
        rightTile = tiling.metersToTileX(Tiling::longitudeToMeters(rightEast));

//        printf("(tiles) Top: %d\nBottom: %d\nLeft: %d\nRight: %d\n", topTile, bottomTile, leftTile, rightTile);

        tileCountX = rightTile - leftTile + 1;
        tileCountY = bottomTile - topTile + 1;

//        printf("Tile count = %d\n", tileCountX * tileCountY);

        targetWidth = tileCountX * tiling.GetPixelsPerTile();
        targetHeight = tileCountY * tiling.GetPixelsPerTile();

        pBand = poDataset->GetRasterBand(1);
//        printf("data type = %d\n", pBand->GetRasterDataType());

        sourceWidth = pBand->GetXSize();
        sourceHeight = pBand->GetYSize();
    }

    inline int longitudeToPixel(double longitude) const {
        return round( (longitude - leftWest) / longitudePerPixel );
    }

    inline int latitudeToPixel(double longitude) const {
        return round( (topNorth - longitude) / longitudePerPixel );
    }


    void render();

    void readImage();

    void project();


    int GetPopulation(int x, int y) {
        if(x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight) {
            return -1;
        } else {
            float pop = pSourceBuffer[(y * sourceWidth) + x];
            return (int)pop;
        }
    }
    void renderTile(int tileX, int tileY);

    void renderTiles();
};


#endif //WORLDPOPTILES_COUNTRY_H
