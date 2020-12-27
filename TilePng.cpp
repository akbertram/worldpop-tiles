
#include <sys/stat.h>
#include <sys/types.h>
#include <png.h>

#include <cstdio>
#include <cerrno>
#include <cstdlib>

#include "TilePng.h"
#include "Tiling.h"

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

void TilePng::writeTile(int zoom, int tileX, int tileY, u_int8_t *pPixels) {
    FILE *fp = nullptr;
    png_structp png_ptr = nullptr;
    png_infop info_ptr = nullptr;
    png_bytep row = nullptr;

    // Open file for writing (binary mode)
    char filename[1024];
    int filenameIndex;
    filenameIndex = sprintf(filename, "tiles/%d", zoom);
    if(mkdir(filename, 0755) != 0 && errno != EEXIST) {
        fprintf(stderr, "Could not create directory %s: %s\n", filename, strerror(errno));
        exit(-1);
    }

    filenameIndex += sprintf(filename + filenameIndex, "/%d", tileX);
    if(mkdir(filename, 0755) != 0 && errno != EEXIST) {
        fprintf(stderr, "Could not create directory %s: %s\n", filename, strerror(errno));
        exit(-1);
    }

    sprintf(filename + filenameIndex, "/%d.png", tileY);

    fp = fopen(filename, "wb");
    if (fp == nullptr) {
        fprintf(stderr, "Could not open file %s for writing\n", filename);
        exit(-1);
    }
    // Initialize write structure
    png_ptr = png_create_write_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr, nullptr);
    if (png_ptr == nullptr) {
        fprintf(stderr, "Could not allocate write struct\n");
        exit(-1);
    }

    // Initialize info structure
    info_ptr = png_create_info_struct(png_ptr);
    if (info_ptr == nullptr) {
        fprintf(stderr, "Could not allocate info struct\n");
        exit(-1);
    }

    // Setup Exception handling
    if (setjmp(png_jmpbuf(png_ptr))) {
        fprintf(stderr, "Error during png creation\n");
        exit(-1);
    }
    png_init_io(png_ptr, fp);

    // Write header (8 bit indexed image)
    png_set_IHDR(png_ptr, info_ptr, Tiling::pixelsPerTile, Tiling::pixelsPerTile,
                 8, PNG_COLOR_TYPE_PALETTE, PNG_INTERLACE_NONE,
                 PNG_COMPRESSION_TYPE_BASE, PNG_FILTER_TYPE_BASE);

    png_set_PLTE(png_ptr, info_ptr, palette, paletteSize);
    png_set_tRNS(png_ptr, info_ptr, paletteTransparency, 2, nullptr);

    png_write_info(png_ptr, info_ptr);

    // Write image data
    for (int y=0 ; y<Tiling::pixelsPerTile ; y++) {
        png_write_row(png_ptr, pPixels + (y * Tiling::pixelsPerTile));
    }

    // End write
    png_write_end(png_ptr, nullptr);

    fclose(fp);
    png_free_data(png_ptr, info_ptr, PNG_FREE_ALL, -1);
    png_destroy_write_struct(&png_ptr, (png_infopp)nullptr);
}

bool TilePng::tryReadTile(int zoom, int tileX, int tileY, u_int8_t *pPixels) {

    char filename[1024];
    sprintf(filename, "tiles/%d/%d/%d.png", zoom, tileX, tileY);

    FILE *pFile = fopen(filename, "rb");
    if(pFile == nullptr) {
        if(errno == ENOENT) {
            return false;
        } else {
            fprintf(stderr, "Error reading tile %s: %s\n", filename, strerror(errno));
            exit(-1);
        }
    }

    png_byte sig[8];
    fread(sig, 1, 8, pFile);
    if (!png_check_sig(sig, 8)) {
        fprintf(stderr, "PNG file with bad signature at %s\n", filename);
        exit(-1);
    }

    png_structp png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, nullptr, nullptr,nullptr);
    if (!png_ptr) {
        fprintf(stderr, "Could not allocate PNG struct\n");
        exit(-1);
    }

    png_infop info_ptr = png_create_info_struct(png_ptr);
    if (!info_ptr) {
        fprintf(stderr, "Could not allocate PNG info struct\n");
        exit(-1);
    }


    png_init_io(png_ptr, pFile);
    png_set_sig_bytes(png_ptr, 8);
    png_read_info(png_ptr, info_ptr);

    png_uint_32 width;
    png_uint_32 height;
    int bit_depth;
    int color_type;
    png_get_IHDR(png_ptr, info_ptr, &width, &height, &bit_depth,
                 &color_type, NULL, NULL, NULL);

    if(width != Tiling::pixelsPerTile || height != Tiling::pixelsPerTile) {
        fprintf(stderr, "Tile image at %s is %ldx%ld, not %dx%d",
                filename,
                width, height,
                Tiling::pixelsPerTile, Tiling::pixelsPerTile);
        exit(-1);
    }
    if(color_type != PNG_COLOR_TYPE_PALETTE && bit_depth != 8) {
        fprintf(stderr, "Tile image at %s does not use an 8-bit palette",
                filename);
        exit(-1);
    }

    for(int row = 0; row < height; row++) {
        png_read_row(png_ptr, pPixels + (row * width), nullptr);
    }

    png_read_end(png_ptr, info_ptr);
    png_destroy_read_struct(&png_ptr, &info_ptr, nullptr);
    fclose(pFile);

    return true;
}
