<!--
 ! Licensed to the Apache Software Foundation (ASF) under one
 ! or more contributor license agreements.  See the NOTICE file
 ! distributed with this work for additional information
 ! regarding copyright ownership.  The ASF licenses this file
 ! to you under the Apache License, Version 2.0 (the
 ! "License"); you may not use this file except in compliance
 ! with the License.  You may obtain a copy of the License at
 !
 !   http://www.apache.org/licenses/LICENSE-2.0
 !
 ! Unless required by applicable law or agreed to in writing,
 ! software distributed under the License is distributed on an
 ! "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ! KIND, either express or implied.  See the License for the
 ! specific language governing permissions and limitations
 ! under the License.
 !-->

## <a id="spatial_joins">Spatial Joins</a>

AsterixDB supports efficient spatial join query performance.
In particular, it will execute the [Partition Based Spatial-Merge Join](http://pages.cs.wisc.edu/~dewitt/includes/paradise/spjoin.pdf)
(PBSM) algorithm for the join queries with a spatial function in join condition.

Supported spatial functions:  
* [`spatial_intersect(ARectangle, ARectangle)`](./geo/functions.html#predicate).
* ESRI's spatial functions:  
    * [`st_intersects`](./geo/functions.html#predicate), 
    * [`st_overlaps`](./geo/functions.html#predicate), 
    * [`st_touches`](./geo/functions.html#predicate), 
    * [`st_contains`](./geo/functions.html#predicate), 
    * [`st_crosses`](./geo/functions.html#predicate), 
    * [`st_within`](./geo/functions.html#predicate), 
    * [`st_distance`](./geo/functions.html#predicate).

Once the join condition contains a supported spatial function, users do not need to do any further action to trigger this efficient query plan.

Here's an example on how we can join two geometry datasets using spatial joins. We will be using the [Chicago Crimes](https://star.cs.ucr.edu/datasets/Chicago%20Crimes/download.json.gz) and [Zip Codes](https://star.cs.ucr.edu/datasets/TIGER2018/ZCTA5/download.json.gz?mbr=-88.07241,41.62385,-87.14613,42.05043) datasets for this example. First, we need to create a dataverse and a datatype before loading the data:
```SQL
DROP DATAVERSE SpatialJoinTest IF EXISTS;
CREATE DATAVERSE SpatialJoinTest;

USE SpatialJoinTest;

CREATE TYPE CrimeType AS {
    `ID`: string,
    `g`: geometry
};

CREATE TYPE ZipCodeType AS {
    `GEOID10`: string,
    `g`: geometry
};
```
Both datasets contains more than the two fields defined above, instead of listing all of them we will make use of the *open* types feature of AsterixDB and define the key and the geometry only for the sake of simplicity. Next, we will create new datasets and start loading the data from the files:
```SQL
USE GeospatialAnalysis;

CREATE DATASET ChicagoCrimes(CrimeType)
    PRIMARY KEY `ID`;

CREATE DATASET ZipCodes(ZipCodeType)
    PRIMARY KEY `GEOID10`;


LOAD DATASET ChicagoCrimes USING localfs(
    ("path"="localhost:///absolute/path/to/Chicago_Crimes.json"),
    ("format"="adm")
);

LOAD DATASET ZipCodes USING localfs(
    ("path"="localhost:///absolute/path/to/TIGER2018_ZCTA5.json"),
    ("format"="adm")
);
```
Now let's say we want to find the Zip Code of the area of each crime in the dataset. To do so, we will run a spatial join query between the two datasets:
```SQL
USE GeospatialAnalysis;

SELECT zc.GEOID10 AS `Zip Code`, cc.`Primary Type` AS `Crime Type`
FROM ChicagoCrimes cc JOIN ZipCodes z ON st_contains(zc.g, cc.g);
```
The query above will join the datasets using the PBSM efficient plan.  

> Note that if there is an index created for the field, AsterixDB will always use the indexed nested-loop join. Thus, the optimization will not be used. Check out the [Performance Tuning](./geo/performance_tuning.html) page for more information.


## <a id="spatial_partitioning_hint">Using a spatial partitioning hint</a>

PBSM algorithm requires the following information to partition data into a grid:  
- The MBR of two input datasets.
- Number of rows and columns of the grid.

By default, the MBR will be computed at running time and the grid size is 100x100.
However, users can also set other values for these parameters using spatial partitioning hint as reducing the grid resolution can be useful if you want to use less memory.


### Spatial partitioning hint example
In this example, assume that MBR of two input datasets is (-180.0, -83.0, 180.0, 90.0) and grid size is 10x10.


    /*+ spatial-partitioning(-180.0, -83.0, 180.0, 90.0, 10, 10) */


## Spatial partitioning hint in a query

```SQL
DROP DATAVERSE test IF EXISTS;
CREATE DATAVERSE test;
USE test;

-- Make GeomType
CREATE TYPE GeomType AS closed {
    id: int32,
    geom: rectangle
};

-- Make Park dataset
CREATE DATASET ParkSet (GeomType) primary key id;

-- Make Lake dataset
CREATE DATASET LakeSet (GeomType) primary key id;

SELECT COUNT(*) FROM ParkSet AS ps, LakeSet AS ls
WHERE /*+ spatial-partitioning(-180.0, -83.0, 180.0, 90.0, 10, 10) */ spatial_intersect(ps.geom, ls.geom);
```
