

#ifndef WORLDPOPTILES_TILEPNG_H
#define WORLDPOPTILES_TILEPNG_H


class TilePng {
public:
    static void WriteTile(int zoom, int tileX, int tileY, u_int8_t * pPixels);
    static bool TryReadTile(int zoom, int tileX, int tileY, u_int8_t *pPixels);
};


#endif //WORLDPOPTILES_TILEPNG_H
