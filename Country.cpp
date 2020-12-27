//
// Created by alex on 26-12-20.
//

#include "Country.h"
#include "Gradient.h"
#include "TilePng.h"

void Country::renderTile(int tileX, int tileY) {

    uint8_t pixelBuffer[tiling.GetPixelsPerTile() * tiling.GetPixelsPerTile()];

    int pixelIndex = 0;
    bool empty = true;

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
            } else {
                pixelBuffer[pixelIndex] = Gradient::transparent;
            }
            pixelIndex++;
        }
    }

    if(!empty) {
        TilePng::writeTile(tiling.GetZoomLevel(), leftTile + tileX, topTile + tileY, pixelBuffer);
    }
}

