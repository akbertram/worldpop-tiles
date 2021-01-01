

#ifndef WORLDPOPTILES_TILERECT_H
#define WORLDPOPTILES_TILERECT_H


class TileRect {
    int leftTile;
    int topTile;
    int tileCountX;
    int tileCountY;

public:
    TileRect() {
    }
    TileRect(const int leftTile, int topTile, int tileCountX, int tileCountY) :
        topTile(topTile),
        leftTile(leftTile),
        tileCountX(tileCountX),
        tileCountY(tileCountY) {
    }

    inline int GetTopTile() const { return topTile; }
    inline int GetBottomTile() const { return topTile + tileCountY - 1; }
    inline int GetLeftTile() const { return leftTile; }
    inline int GetRightTile() const { return leftTile + tileCountX - 1; };


    inline int GetTileCountX() const { return tileCountX; }
    inline int GetTileCountY() const { return tileCountY; }
    inline int GetTileCount() const { return tileCountX * tileCountY; }

};


#endif //WORLDPOPTILES_TILERECT_H
