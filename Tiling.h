
#ifndef WORLDPOPTILES_TILING_H
#define WORLDPOPTILES_TILING_H

#include <cmath>

class Tiling {
public:
    static constexpr double RADIUS = 6378137.0;
    static constexpr double PROJ_MIN = - M_PI * RADIUS;
    static constexpr double PROJ_MAX = + M_PI * RADIUS;
    static constexpr double PROJ_SIZE = PROJ_MAX - PROJ_MIN;
    static const int pixelsPerTile = 256;
    static constexpr double originShift = 2 * M_PI * RADIUS / 2.0;

    static int tileCountAtZoomLevel(int zoomLevel) {
        return (int)pow(2, zoomLevel);
    }

private:
    int tileCount;
    int zoomLevel;
    double metersPerTile;
    double pixelsPerMeter;
    int worldSizePixels;
    double metersPerPixel;

public:
    Tiling(int zoomLevel) {
        this->tileCount = tileCountAtZoomLevel(zoomLevel);
        this->zoomLevel = zoomLevel;
        this->metersPerTile = PROJ_SIZE / tileCount;
        this->worldSizePixels = (int) (tileCount * pixelsPerTile);
        this->pixelsPerMeter = worldSizePixels / PROJ_SIZE;
        this->metersPerPixel = PROJ_SIZE / worldSizePixels;
    }


    static double radiansToDegrees(double rad) {
        return rad * 180.0 / M_PI;
    }

    static double degreesToRadians(double deg) {
        return deg / 180.0 * M_PI;
    }

    static double latitudeToMeters(double latitude) {
        double my = log( tan((90 + latitude) * M_PI / 360.0 )) / (M_PI / 180.0);
        my = my * originShift / 180.0;
        return my;
    }

    static double longitudeToMeters(double longitude) {
        return longitude * originShift / 180.0;
    }

    static double metersToLongitude(double aX) {
        return (aX / RADIUS) * 180.0 / M_PI;
    }

    static double metersToLatitude(double aY) {
        return radiansToDegrees(atan(exp(aY / RADIUS)) * 2 - M_PI/2);
    }


    int metersToTileX(double metersX) {
        return floor((metersX - PROJ_MIN) / metersPerTile);
    }

    int metersToTileY(double metersY) {
        return floor( (PROJ_MAX - metersY) / metersPerTile);
    }

    inline int GetPixelsPerTile() {
        return pixelsPerTile;
    }

    inline double GetMetersPerPixel() {
        return this->metersPerPixel;
    }

    /**
     * The position, in meters, of the tile's left edge.
     */
    double meterTileLeft(int tileX) {
        return PROJ_MIN + (tileX * metersPerTile);
    }

    double meterTileRight(int tileX) {
        return meterTileLeft(tileX) + metersPerTile;
    }

    /*
     * The position, in meters, of the tile's top edge.
     */
    double meterTileTop(int tileY) {
        return PROJ_MAX - (tileY * metersPerTile);
    }

    /**
     * The position, in meters, of the tile's bottom edge.
     */
    double meterTileBottom(int tileY) {
        return meterTileTop(tileY) - metersPerTile;
    }

    int GetZoomLevel() {
        return zoomLevel;
    }
};


#endif //WORLDPOPTILES_TILING_H
