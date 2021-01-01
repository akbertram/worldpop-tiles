
#ifndef WORLDPOPTILES_TILING_H
#define WORLDPOPTILES_TILING_H

#include <cmath>
#include "TileRect.h"
#include "GeoRect.h"

class Tiling {
public:
    static constexpr double RADIUS = 6378137.0;
    static constexpr double PROJ_MIN = - M_PI * RADIUS;
    static constexpr double PROJ_MAX = + M_PI * RADIUS;
    static constexpr double PROJ_SIZE = PROJ_MAX - PROJ_MIN;
    static const int pixelsPerTile = 256;
    static constexpr double originShift = 2 * M_PI * RADIUS / 2.0;

    static int TileCountAtZoomLevel(int zoomLevel) {
        return (int)pow(2, zoomLevel);
    }

private:
    int tileCount;
    int zoomLevel;
    double metersPerTile;
    int worldSizePixels;
    double metersPerPixel;

public:
    Tiling(int zoomLevel) : zoomLevel(zoomLevel) {
        tileCount = TileCountAtZoomLevel(zoomLevel);
        metersPerTile = PROJ_SIZE / tileCount;
        worldSizePixels = (int) (tileCount * pixelsPerTile);
        metersPerPixel = PROJ_SIZE / worldSizePixels;
    }


    static double RadiansToDegrees(double rad) {
        return rad * 180.0 / M_PI;
    }

    static double LatitudeToMeters(double latitude) {
        double my = log( tan((90 + latitude) * M_PI / 360.0 )) / (M_PI / 180.0);
        my = my * originShift / 180.0;
        return my;
    }

    static double LongitudeToMeters(double longitude) {
        return longitude * originShift / 180.0;
    }

    static double MetersToLongitude(double aX) {
        return (aX / RADIUS) * 180.0 / M_PI;
    }

    static double MetersToLatitude(double aY) {
        return RadiansToDegrees(atan(exp(aY / RADIUS)) * 2 - M_PI / 2);
    }


    int MetersToTileX(double metersX) const {
        return floor((metersX - PROJ_MIN) / metersPerTile);
    }

    int MetersToTileY(double metersY) const {
        return floor( (PROJ_MAX - metersY) / metersPerTile);
    }

    inline double GetMetersPerPixel() {
        return metersPerPixel;
    }

    TileRect GeographicRectToTileRect(double topNorth, double bottomSouth, double leftWest, double rightEast) {

        int topTile = MetersToTileY(Tiling::LatitudeToMeters(topNorth));
        int bottomTile = MetersToTileY(Tiling::LatitudeToMeters(bottomSouth));
        int leftTile = MetersToTileX(Tiling::LongitudeToMeters(leftWest));
        int rightTile = MetersToTileX(Tiling::LongitudeToMeters(rightEast));

        return TileRect(leftTile, topTile, rightTile - leftTile + 1, bottomTile - topTile + 1);
    }

    /**
     * The position, in meters, of the tile's left edge.
     */
    double MeterTileLeft(int tileX) {
        return PROJ_MIN + (tileX * metersPerTile);
    }

    double MeterTileRight(int tileX) {
        return MeterTileLeft(tileX) + metersPerTile;
    }

    /*
     * The position, in meters, of the tile's top edge.
     */
    double MeterTileTop(int tileY) {
        return PROJ_MAX - (tileY * metersPerTile);
    }

    /**
     * The position, in meters, of the tile's bottom edge.
     */
    double meterTileBottom(int tileY) {
        return MeterTileTop(tileY) - metersPerTile;
    }

    int GetZoomLevel() const {
        return zoomLevel;
    }

    GeoRect TileRectToGeoRect(const TileRect &rect) {
        double longitudeWestLeft = MetersToLongitude(MeterTileLeft(rect.GetLeftTile()));
        double latitudeNorth = MetersToLatitude(MeterTileTop(rect.GetTopTile()));
        double longitudeEastRight = MetersToLongitude(MeterTileRight(rect.GetRightTile()));
        double latitudeSouth = MetersToLatitude(meterTileBottom(rect.GetBottomTile()));

        return GeoRect(latitudeNorth, latitudeSouth, longitudeEastRight, longitudeWestLeft);
    }
};


#endif //WORLDPOPTILES_TILING_H
