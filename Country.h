

#ifndef WORLDPOPTILES_COUNTRY_H
#define WORLDPOPTILES_COUNTRY_H

#include <png.h>
#include <boost/filesystem.hpp>
#include "gdal_priv.h"
#include "Tiling.h"

class Country {
    boost::filesystem::path path;
    Tiling tiling;
    double longitudePerPixel;
    double latitudePerPixel;
    double topNorth;
    double bottomSouth;
    double leftWest;
    double rightEast;

    TileRect tileRect;

    int width;
    int height;

public:
    Country(Tiling &rTiling, boost::filesystem::path path) : path(path), tiling(rTiling) {
        GDALDataset *poDataset = (GDALDataset *) GDALOpen(path.c_str(), GA_ReadOnly);
        if(poDataset == nullptr) {
            return;
        }
        width = poDataset->GetRasterXSize();
        height = poDataset->GetRasterYSize();

        double adfGeoTransform[6];
        poDataset->GetGeoTransform(adfGeoTransform);
        longitudePerPixel = adfGeoTransform[1];
        latitudePerPixel = adfGeoTransform[5];

        topNorth = adfGeoTransform[3];
        bottomSouth = topNorth + (height * latitudePerPixel);
        leftWest = adfGeoTransform[0];
        rightEast = leftWest + (width * longitudePerPixel);

        tileRect = tiling.GeographicRectToTileRect(topNorth, bottomSouth, leftWest, rightEast);

        delete poDataset;
    }

    inline double longitudeToPixel(double longitude) const {
        return (longitude - leftWest) / longitudePerPixel;
    }

    inline double latitudeToPixel(double longitude) const {
        return (topNorth - longitude) / longitudePerPixel;
    }

    inline Tiling GetTiling() const {
        return tiling;
    }

    inline int GetWidth() const {
        return width;
    }

    inline int GetHeight() const {
        return height;
    }

    const boost::filesystem::path &GetPath() const {
        return path;
    }

    std::vector<TileRect> DivideIntoBatches() {

        // We are assuming that the input tiffs have blocks of 1 pixel high. For this reason,
        // it makes sense to divide them into batches of horizontal bands, depending on the width
        // of the image

        int batchSize = 16384 / tileRect.GetTileCountX();
        if(batchSize < 1) {
            batchSize = 1;
        }

        std::vector<TileRect> rects;
        for(int top=tileRect.GetTopTile();top < tileRect.GetBottomTile(); top+= batchSize) {
            int tileCountY = batchSize;
            if(top + tileCountY - 1 > tileRect.GetBottomTile()) {
                tileCountY = tileRect.GetBottomTile() - top + 1;
            }
            rects.push_back(TileRect(tileRect.GetLeftTile(), top, tileRect.GetTileCountX(), tileCountY));
        }
        return rects;
    }
};


#endif //WORLDPOPTILES_COUNTRY_H
