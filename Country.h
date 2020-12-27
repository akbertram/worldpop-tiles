

#ifndef WORLDPOPTILES_COUNTRY_H
#define WORLDPOPTILES_COUNTRY_H

#include <png.h>
#include "gdal_priv.h"
#include "Tiling.h"

class Country {

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
    Country(Tiling &rTiling, const char *pszFilename) : tiling(rTiling) {
        poDataset = (GDALDataset *) GDALOpen( pszFilename, GA_ReadOnly );
        if(poDataset == nullptr) {
            return;
        }
        double adfGeoTransform[6];
        poDataset->GetGeoTransform(adfGeoTransform);
        this->longitudePerPixel = adfGeoTransform[1];
        this->latitudePerPixel = adfGeoTransform[5];

        this->topNorth = adfGeoTransform[3];
        this->bottomSouth = topNorth + (poDataset->GetRasterYSize() * this->latitudePerPixel);

        this->leftWest = adfGeoTransform[0];
        this->rightEast = this->leftWest + (poDataset->GetRasterXSize() * this->longitudePerPixel);

        printf("North: %f\nSouth: %f\nWest: %f\nEast: %f\n", this->topNorth, this->bottomSouth, this->leftWest, this->rightEast);

        this->topTile = tiling.metersToTileY(Tiling::latitudeToMeters(topNorth));
        this->bottomTile = tiling.metersToTileY(Tiling::latitudeToMeters(bottomSouth));
        this->leftTile = tiling.metersToTileX(Tiling::longitudeToMeters(leftWest));
        this->rightTile = tiling.metersToTileX(Tiling::longitudeToMeters(rightEast));

        printf("(tiles) Top: %d\nBottom: %d\nLeft: %d\nRight: %d\n", this->topTile, this->bottomTile, this->leftTile, this->rightTile);

        this->tileCountX = this->rightTile - this->leftTile + 1;
        this->tileCountY = this->bottomTile - this->topTile + 1;

        printf("Tile count = %d\n", tileCountX * tileCountY);

        this->targetWidth = this->tileCountX * this->tiling.GetPixelsPerTile();
        this->targetHeight = this->tileCountY * this->tiling.GetPixelsPerTile();

        this->pBand = poDataset->GetRasterBand(1);
        printf("data type = %d\n", pBand->GetRasterDataType());

        this->sourceWidth = pBand->GetXSize();
        this->sourceHeight = pBand->GetYSize();
    }

    int longitudeToPixel(double longitude) const {
        return round( (longitude - this->leftWest) / longitudePerPixel );
    }

    int latitudeToPixel(double longitude) const {
        return round( (this->topNorth - longitude) / longitudePerPixel );
    }


    void renderTiles() {
        readImage();
        project();
        for (int tileX = 0; tileX < tileCountX; tileX++) {
            for (int tileY = 0; tileY < tileCountY; tileY++) {
                renderTile(tileX, tileY);
            }
        }
    }

    void readImage() {
        pSourceBuffer = (float *) CPLMalloc(sizeof(float)*(sourceWidth * sourceHeight));
        pBand->RasterIO(GF_Read,
                        0, 0, sourceWidth, sourceHeight, /* source rectangle */
                        pSourceBuffer,
                        sourceWidth,
                        sourceHeight,
                        GDT_Float32,
                        sizeof(float), /* nPixelSpace */
                        sizeof(float) * sourceWidth, /* nLineSpace (scanline) */
                        nullptr);
    }

    void project() {
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
    }


    int GetPopulation(int x, int y) {
        if(x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight) {
            return -1;
        } else {
            float pop = pSourceBuffer[(y * sourceWidth) + x];
            return (int)pop;
        }
    }
    void renderTile(int tileX, int tileY);
    void writePNG(unsigned int * pPixels, int tileX, int tileY);

};


#endif //WORLDPOPTILES_COUNTRY_H
