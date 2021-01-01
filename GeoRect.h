

#ifndef WORLDPOPTILES_GEORECT_H
#define WORLDPOPTILES_GEORECT_H


class GeoRect {
    double north;
    double south;
    double east;
    double west;

public:
    GeoRect(double north, double south, double east, double west) :
        north(north), south(south), east(east), west(west) {
    }

    inline double GetNorth() { return north; }
    inline double GetSouth() { return south; }
    inline double GetEast() { return east; }
    inline double GetWest() { return west; }
    inline double GetLeft() { return west; }
    inline double GetRight() { return east; }
    inline double GetTop() { return north; }
    inline double GetBottom() { return south; }
};


#endif //WORLDPOPTILES_GEORECT_H
