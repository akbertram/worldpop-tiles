//
// Created by alex on 27-12-20.
//

#ifndef WORLDPOPTILES_TILEPNG_H
#define WORLDPOPTILES_TILEPNG_H


class TilePng {
public:
    static void writeTile(int zoom, int tileX, int tileY, u_int8_t * pPixels);
    static bool tryReadTile(int zoom, int tileX, int tileY, u_int8_t *pPixels);
};


#endif //WORLDPOPTILES_TILEPNG_H
