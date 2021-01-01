

#include <cstdlib>
#include <png.h>
#include "Tiling.h"
#include "Gradient.h"
#include "TilePng.h"
#include "DownSampler.h"


DownSampler::DownSampler(int zoom, int tileX, int tileY) : zoom(zoom), tileX(tileX), tileY(tileY) {
}

void DownSampler::Run() {

    int nPixels = Tiling::pixelsPerTile * Tiling::pixelsPerTile;
    u_int8_t input[nPixels];
    u_int8_t output[nPixels];
    memset(output, Gradient::transparent, nPixels);

    bool empty = true;

    for(int sx = 0; sx < 2; sx++) {
        for(int sy = 0; sy < 2; sy++) {
            if(TilePng::TryReadTile(zoom + 1, tileX * 2 + sx, tileY * 2 + sy, input)) {
                empty = false;
                DownSampleQuad(input, output, sx * Tiling::pixelsPerTile / 2, sy * Tiling::pixelsPerTile / 2);
            }
        }
    }
    if(!empty) {
        TilePng::WriteTile(zoom, tileX, tileY, output);
    }
}


void DownSampler::DownSampleQuad(const u_int8_t *input, u_int8_t *output, int offsetX, int offsetY) {

    for(int y=0; y < Tiling::pixelsPerTile / 2; y++) {
        for(int x=0;x < Tiling::pixelsPerTile / 2; x++) {
            float sum = 0;
            int count = 0;
            for(int sx = 0; sx < 2; sx++) {
                for(int sy = 0; sy < 2; ++sy) {
                    int inputX = (x * 2) + sx;
                    int inputY = (y * 2) + sy;
                    int inputIndex = (inputY * Tiling::pixelsPerTile) + inputX;
                    int color = input[inputIndex];
                    if(color != 0) {
                        count++;
                        sum += Gradient::ColorIndexToPopulation(color);
                    }
                }
            }
            if(count >= 2) {
                float averagePopulation = sum / (float) 4;
                int outputColor = Gradient::PopulationToColorIndex(round(averagePopulation));
                int outputIndex = ((offsetY + y) * Tiling::pixelsPerTile) + (offsetX + x);
                output[outputIndex] = outputColor;
            }
        }
    }
}
