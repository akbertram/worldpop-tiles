
#ifndef WORLDPOPTILES_GRADIENT_H
#define WORLDPOPTILES_GRADIENT_H

#include <png.h>

class Gradient {
public:
    static const u_int8_t transparent = 0;


public:
    inline static u_int8_t PopulationToColorIndex(int pop) {
        if(pop < 0) {
            return 0;
        }
        if(pop < 1) {
            return 1;
        }
        if(pop < 4) {
            return 2;
        }
        if(pop < 8) {
            return 3;
        }
        if(pop < 12) {
            return 4;
        }
        if(pop < 20) {
            return 5;
        }
        if(pop < 50) {
            return 6;
        }
        if(pop < 100) {
            return 7;
        }
        if(pop < 3000) {
            return 8;
        }
        return 9;
    }

    inline static float ColorIndexToPopulation(int color) {
        switch (color) {
            default:
            case 0:
                return 0;
            case 2:
                return 2;
            case 3:
                return 6;
            case 4:
                return 10;
            case 5:
                return 16;
            case 6:
                return 35;
            case 7:
                return 75;
            case 8:
                return 2000;
            case 9:
                return 5000;
        }
    }
};


#endif //WORLDPOPTILES_GRADIENT_H
