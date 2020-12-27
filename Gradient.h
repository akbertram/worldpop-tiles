//
// Created by alex on 27-12-20.
//

#ifndef WORLDPOPTILES_GRADIENT_H
#define WORLDPOPTILES_GRADIENT_H



class Gradient {
public:
    static const unsigned int transparent = 0xffffff;

    constexpr inline static unsigned int rgb(int r, int g, int b) {
        return r | (g << 8) | (b << 16) | 0xff000000;
    }

    inline static unsigned int color(float pop) {
        if(pop < 0) {
            return transparent;
        }
        if(pop < 1) {
            return rgb(255, 255, 240);
        }
        if(pop < 4) {
            return rgb(255, 255, 204);
        }
        if(pop < 8) {
            return rgb(254, 237, 160);
        }
        if(pop < 12) {
            return rgb(254, 217, 118);
        }
        if(pop < 20) {
            return rgb(254, 178, 76);
        }
        if(pop < 50) {
            return rgb(253, 141, 60);
        }
        if(pop < 100) {
            return rgb(252, 78, 42);
        }
        if(pop < 3000) {
            return rgb(227, 26, 28);
        }
        return rgb(177, 0, 38);
    }
};


#endif //WORLDPOPTILES_GRADIENT_H
