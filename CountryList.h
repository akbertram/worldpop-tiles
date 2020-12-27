
#ifndef WORLDPOPTILES_COUNTRYLIST_H
#define WORLDPOPTILES_COUNTRYLIST_H

#include <string>
#include <vector>
#include "Country.h"

class CountryList {
private:
public:
    std::vector<Country> countries;
    CountryList(Tiling &tiling, std::string directory);
};


#endif //WORLDPOPTILES_COUNTRYLIST_H
