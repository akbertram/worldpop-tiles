

#ifndef WORLDPOPTILES_DOWNSAMPLER_H
#define WORLDPOPTILES_DOWNSAMPLER_H


class DownSampler {

    int zoom;
    int tileX;
    int tileY;

public:
    DownSampler(int zoom, int tileX, int tileY);
    void downSample();

private:
    void downSampleQuad(const u_int8_t* input, u_int8_t* output, int offsetX, int offsetY);
};


#endif //WORLDPOPTILES_DOWNSAMPLER_H
