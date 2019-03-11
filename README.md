# automated-redistricting

1. Create the following folder structure
  {Project Folder - any name you want}
      - data
      - jars
      - logs
      - scripts
      - output
2. You will need to download the following shape files into the /data folder
      - [Census Tracts](https://www.census.gov/cgi-bin/geo/shapefiles/index.php?year=2010&layergroup=Census+Tracts)
        - Note Census Tract Shape files do not include population numbers. You will need to download the [Tract Relationship Files](https://www.census.gov/geo/maps-data/data/tract_rel_download.html) and name the files "tract-pop.txt". Each of these text files should be placed inside the shape file folder: /data/tl_2010_xx_tract10/tract-pop.txt
      - [Census Blocks](https://www.census.gov/cgi-bin/geo/shapefiles/index.php?year=2010&layergroup=Blocks)
      - [State Geography](https://www.census.gov/cgi-bin/geo/shapefiles/index.php?year=2010&layergroup=States+%28and+equivalent%29)
      - Actual Congressional districts need to be stored in the following directory "data/2012Congress/2012Congress.shp". You are welcome to use benchmarks other than the 2012 districts but it will need to have this name or you will need to change the code.
      - **Do not rename the files; use the default names from the U.S. Census**
3. Download dac.jar into the /jars folder
      - you can modify the code in this repository and create a new jar if you would like
4. Download dac.sh into the /scripts folder
      - you will want to modify this file to fit your needs
      - currently it runs just NY and FL, but you can remove the if statement if you want to run all of the states
      - MAX_FUNC should be set to the max function (pop, both, contig)
      - UNIT should be set to the data that you selected (tract, block)
      - SWAP should be set to enable or disable population optimization (true, false)
      - SITE should be set to point (this is a potential feature for a future release)
  
 5. Run the code and view the results and log files


All results are available as equivalency files online: https://bitbucket.org/hlevin/redistricting-results
