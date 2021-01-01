cmake_minimum_required(VERSION 3.5)
project(worldpoptiles)

set(CMAKE_CXX_STANDARD 14)

# find system installed GDAL package with predefined CMake variable for finding GDAL
find_package(GDAL REQUIRED)
find_package(PNG REQUIRED)
find_package(Boost COMPONENTS filesystem timer REQUIRED )

# Specify location of header files
include_directories( include ${GDAL_INCLUDE_DIRS})
include_directories( ${PNG_INCLUDE_DIR})
include_directories( ${Boost_INCLUDE_DIR} )

add_executable(worldpoptiles main.cpp Country.h Tiling.h Gradient.h DownSampler.cpp DownSampler.h TilePng.cpp TilePng.h TileBatch.cpp TileBatch.h TileRect.h GeoRect.h)

target_link_libraries(worldpoptiles ${GDAL_LIBRARIES})
target_link_libraries(worldpoptiles ${PNG_LIBRARY})
target_link_libraries(worldpoptiles ${Boost_LIBRARIES} )