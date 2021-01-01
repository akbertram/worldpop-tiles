

#ifndef WORLDPOPTILES_DOWNSAMPLER_H
#define WORLDPOPTILES_DOWNSAMPLER_H

#include "Task.h"

class DownSampler : public Task {

    int zoom;
    int tileX;
    int tileY;

public:
    DownSampler(int zoom, int tileX, int tileY);
    void Run();

private:
    void DownSampleQuad(const u_int8_t* input, u_int8_t* output, int offsetX, int offsetY);
};


#endif //WORLDPOPTILES_DOWNSAMPLER_H
