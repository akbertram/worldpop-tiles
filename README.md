
# World Pop Tiled Datasets

This repository contains a set of scripts and tools
to download, project, resample, tile, and publish the gridded WorldPop
data.

## Source

This generates tiles from WorldPop's 100m resolution [Unconstrained individual countries](https://www.worldpop.org/geodata/listing?id=29).

## Prerequisites

These tools have been written to run on Linux, and require the GDAL
library, as well as the Java bindings for GDAL.

On Ubuntu, you can install the prerequisites via:

    sudo add-apt-repository ppa:ubuntugis/ppa
    sudo apt-get update
    sudo apt-get install wget gdal-bin libgdal-java


## Configuration

The following environment variables govern the tooling:

  * `SOURCE_DIR`: the directory containing the downloaded 100m-resolution
    images
  * `GCS_TILE_BUCKET`: the name of the Google Cloud Storage bucket
  * `GCS_TILE_PREFIX`: the prefix for the tile object name
  * `GCS_THREADS`: the number of threads to use for uploading


## License

This repository is licensed under the GNU Affero General Public License






