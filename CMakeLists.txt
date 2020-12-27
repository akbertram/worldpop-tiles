cmake_minimum_required(VERSION 3.17)
project(worldpoptiles)

set(CMAKE_CXX_STANDARD 14)

# find system installed GDAL package with predefined CMake variable for finding GDAL
find_package(GDAL REQUIRED)
find_package(PNG REQUIRED)

# Specify location of GDAL header files
include_directories( include ${GDAL_INCLUDE_DIRS})
include_directories( ${PNG_INCLUDE_DIR})

add_executable(worldpoptiles main.cpp Country.cpp Country.h Tiling.h Gradient.h)

# Specify GDAL libraries to link your cpp executable target against
target_link_libraries(worldpoptiles ${GDAL_LIBRARIES})
target_link_libraries(worldpoptiles ${PNG_LIBRARY})