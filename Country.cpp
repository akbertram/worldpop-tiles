//
// Created by alex on 26-12-20.
//

#include "Country.h"
#include "Gradient.h"
#include <sys/stat.h>
#include <sys/types.h>
#include <errno.h>
#include <string.h>

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
        writePNG(pixelBuffer, leftTile + tileX, topTile + tileY);
    }
}

int paletteSize = 10;

png_color palette[] = {
        {255, 255, 255},
        {255, 255, 240},
        {255, 255, 204},
        {254, 237, 160},
        {254, 217, 118},
        {254, 178, 76},
        {253, 141, 60},
        {252, 78, 42},
        {227, 26, 28},
        {177, 0, 38} };

png_byte paletteTransparency[] = {0, 255, 255, 255, 255, 255, 255, 255, 255, 255 };

void Country::writePNG(uint8_t *pPixels, int tileX, int tileY) {
    FILE *fp = nullptr;
    png_structp png_ptr = nullptr;
    png_infop info_ptr = nullptr;
    png_bytep row = nullptr;

    // Open file for writing (binary mode)
    char filename[1024];
    int filenameIndex;
    filenameIndex = sprintf(filename, "tiles/%d", tiling.GetZoomLevel());
    if(mkdir(filename, 0755) != 0 && errno != EEXIST) {
        fprintf(stderr, "Could not create directory %s: %s\n", filename, strerror(errno));
        goto finalise;
    }

    filenameIndex += sprintf(filename + filenameIndex, "/%d", tileX);
    if(mkdir(filename, 0755) != 0 && errno != EEXIST) {
        fprintf(stderr, "Could not create directory %s: %s\n", filename, strerror(errno));
        goto finalise;
    }

    sprintf(filename + filenameIndex, "/%d.png", tileY);

    fp = fopen(filename, "wb");
    if (fp == nullptr) {
        fprintf(stderr, "Could not open file %s for writing\n", filename);
        goto finalise;
    }
    // Initialize write structure
    png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    if (png_ptr == nullptr) {
        fprintf(stderr, "Could not allocate write struct\n");
        goto finalise;
    }

    // Initialize info structure
    info_ptr = png_create_info_struct(png_ptr);
    if (info_ptr == nullptr) {
        fprintf(stderr, "Could not allocate info struct\n");
        goto finalise;
    }

    // Setup Exception handling
    if (setjmp(png_jmpbuf(png_ptr))) {
        fprintf(stderr, "Error during png creation\n");
        goto finalise;
    }
    png_init_io(png_ptr, fp);

    // Write header (8 bit indexed image)
    png_set_IHDR(png_ptr, info_ptr, Tiling::pixelsPerTile, Tiling::pixelsPerTile,
                 8, PNG_COLOR_TYPE_PALETTE, PNG_INTERLACE_NONE,
                 PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);

    png_set_PLTE(png_ptr, info_ptr, palette, paletteSize);
    png_set_tRNS(png_ptr, info_ptr, paletteTransparency, 2, NULL);

    png_write_info(png_ptr, info_ptr);

    // Write image data
    for (int y=0 ; y<Tiling::pixelsPerTile ; y++) {
        png_write_row(png_ptr, reinterpret_cast<png_bytep>(pPixels + (y * Tiling::pixelsPerTile)));
    }

    // End write
    png_write_end(png_ptr, nullptr);

    finalise:
    if (fp != nullptr) {
        fclose(fp);
    }
    if (info_ptr != nullptr) {
        png_free_data(png_ptr, info_ptr, PNG_FREE_ALL, -1);
    }
    if (png_ptr != nullptr) {
        png_destroy_write_struct(&png_ptr, (png_infopp)nullptr);
    }
}
