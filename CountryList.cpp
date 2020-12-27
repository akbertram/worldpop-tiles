
#include "CountryList.h"
#include <boost/filesystem.hpp>
#include <cstdio>
#include <iostream>

using namespace boost::filesystem;
using namespace std;

CountryList::CountryList(Tiling &tiling, std::string directory) {
    for (directory_entry& entry : directory_iterator(path(directory))) {
        cout << "    " << entry.path() << '\n';
        countries.emplace_back(tiling, entry.path());
    }
}
