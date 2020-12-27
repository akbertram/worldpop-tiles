//
// Created by alex on 27-12-20.
//

#ifndef WORLDPOPTILES_GRADIENT_H
#define WORLDPOPTILES_GRADIENT_H

#include <png.h>

class Gradient {
public:
    static const uint8_t transparent = 0;


public:
    inline static uint8_t PopulationToColorIndex(int pop) {
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
};


#endif //WORLDPOPTILES_GRADIENT_H
