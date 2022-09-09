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

# Getting Started with GIS in AsterixDB #
## <a id="toc">Table of Contents</a> ##

* [Introduction](#Introduction)
* [Create a GIS Data Type](#create)
* [Insert Geometry Data](#update)
* [Query Geometries](#query)
* [Spatial Analysis Function](#query2)
* [Spatial Aggregate Example](#aggre)
* [Range Query](#range)
* [K Nearest Neighbor (KNN) Query](#knn)
* [Spatial Join Query](#joint)

## <a id="Introduction">Introduction</a>
This page provides a simple guide to the OGC-compliant geometry functionality in AsterixDB. Internally, AsterixDB relies on the open source library [Esri/geometry-api-java](https://github.com/Esri/geometry-api-java) that provides OGC-geometry feature processing. Currently, the AsterixDB geometry library supports
[GeoJSON](https://tools.ietf.org/html/rfc7946), [Well known Text (WKT)](http://docs.opengeospatial.org/is/12-063r5/12-063r5.html)
and [Well known Binary (WKB)](http://portal.opengeospatial.org/files/?artifact_id=25354) formats.
For a complete list of all the functions, please check the [AsterixDB GIS functions page](functions.html).
Here are some detailed examples.
 

## <a id="create">Create a GIS Data Type</a>

The GIS data can be represented by multiple types (*Point*, *LineString*, *Polygon*, etc..) that describe distinct geometries. These types are defined in AsterixDB under a single primitive datatype called the *geometry* datatype that can store and manage all of the GIS data.  
Below is an example of how you can include *geometry* data type in a custom type and use it to create a new dataset. In this example we will create a new dataverse called `GISTest` where we will add a datatype called `GeometryType` then use that datatype to create a dataset called `Geometries`.
```SQL
DROP DATAVERSE GISTest IF EXISTS;
CREATE DATAVERSE GISTest;
USE GISTest;
 
CREATE TYPE GeometryType AS {
  id : int,
  myGeometry : geometry
};
 
CREATE DATASET Geometries (GeometryType) PRIMARY KEY id;
```
The first three lines above tell AsterixDB to drop the old `GISTest` dataverse, if one already exists, and then to create a brand new one and make it the focus of the statements that follow. Then  the `CREATE TYPE` statement creates a datatype for holding information about geometries and have an id for each object. It is an object type with *integer* and *geometry* data. The indicated fields are all mandatory, but because the type is open, additional fields can be added if present. 
The final line in the code block above creates a dataset for holding our GIS data in the `GISTest` dataverse. The CREATE DATASET statement creates the `Geometries` data set. It specifies that this dataset will store data instances conforming to GeometryType and that it has a primary key which is the id field of each instance. The primary key information is used by AsterixDB to uniquely identify instances for the purpose of later lookup and for use in secondary indexes.


## <a id="update">Insert Geometry Data</a>

After creating the datatype and the dataset in the previous section, we will now go through two examples of inserting data into our new dataset.  

The first example shows how to insert the data manually to the dataset using the `INSERT INTO` statement and creating the *geometry* objects using functions like `st_geom_from_geojson`, `st_geom_from_text`, and `st_geom_from_wkb`. For a complete list of all the construction functions that can be used to create geometry types along with its documentation, please check the Constrution Functions section in the [AsterixDB GIS functions page](functions.html)<!-- (functions.md#construction)-->.
```SQL
USE GISTest;

INSERT INTO Geometries ([
  {"id": 123, "myGeometry": st_geom_from_geojson({"type":"Point","coordinates":[-118.4,33.93]})},
  {"id": 124, "myGeometry": st_geom_from_geojson({"type":"Polygon","coordinates":[[[8.7599721,49.7103028],[8.759997,49.7102752],[8.7600145,49.7102818],[8.7600762,49.7102133],[8.760178,49.7102516],[8.7600914,49.7103478],[8.7599721,49.7103028]]]})},
  {"id": 126, "myGeometry": st_geom_from_geojson({"type":"LineString","coordinates":[[-69.1991349,-12.6006222],[-69.199136,-12.599842],[-69.1982979,-12.5998268],[-69.1982598,-12.599869],[-69.1982188,-12.5998698],[-69.19817,-12.5998707],[-69.198125,-12.5998218],[-69.1973024,-12.5998133],[-69.1972972,-12.6003109],[-69.197394,-12.6003514],[-69.1973906,-12.6009231],[-69.1975115,-12.601026],[-69.1975081,-12.6010968]]})},
  {"id": 127, "myGeometry": st_geom_from_geojson({"type": "MultiPoint","coordinates": [[10, 40], [40, 30], [20, 20], [30, 10]]})},
  {"id": 128, "myGeometry": st_geom_from_geojson({"type": "MultiLineString","coordinates": [[[10, 10], [20, 20], [10, 40]],[[40, 40], [30, 30], [40, 20], [30, 10]]]})},
  {"id": 129, "myGeometry": st_geom_from_geojson({"type": "MultiPolygon","coordinates": [[[[40, 40], [20, 45], [45, 30], [40, 40]]],[[[20, 35], [10, 30], [10, 10], [30, 5], [45, 20], [20, 35]],[[30, 20], [20, 15], [20, 25], [30, 20]]]]})},
  {"id": 130, "myGeometry": st_make_point(-71.1043443253471, 42.3150676015829)},
  {"id": 131, "myGeometry": st_make_point(1.0,2.0,3.0)},
  {"id": 132, "myGeometry": st_make_point(1.0,2.0,3.0,4.0)},
  {"id": 133, "myGeometry": st_geom_from_text('POLYGON((743238 2967416,743238 2967450,743265 2967450,743265.625 2967416,743238 2967416))')},
  {"id": 134, "myGeometry": st_geom_from_wkb(hex("0102000000020000001F85EB51B87E5CC0D34D621058994340105839B4C87E5CC0295C8FC2F5984340"))},
  {"id": 135, "myGeometry": st_line_from_multipoint(st_geom_from_text('MULTIPOINT(1 2 , 4 5 , 7 8 )'))},
  {"id": 136, "myGeometry": st_make_envelope(10, 10, 11, 11, 4326)},
  {"id": 137, "myGeometry": st_geom_from_text("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))")}
]);
```
This example shows different types and representations of geometries and how they are inserted to the dataset.  

The second example shows how to load data from a file and insert it into our dataset. We first remove the dataset if it exists as a dataset can currently only be loaded if it is empty. Then we load the dataset file using the `LOAD DATASET` statement. You can download the example file [here](./example_data.json).
```SQL
USE GISTest;

DROP DATASET Geometries IF EXISTS;
CREATE DATASET Geometries (GeometryType) PRIMARY KEY id;

LOAD DATASET Geometries USING localfs(
  ("path"="localhost:///home/malin/Projects/gsoc/asterixdb/asterixdb/asterix-doc/src/site/markdown/geo/example_data.json"),
  ("format"="adm")
);
```

## <a id="query">Query Geometries</a>

The following statements below show how you can use queries to examine the dataset you have created.
```SQL
USE GISTest;

FROM Geometries SELECT *;
```
Result:
```JSON
{"Geometries":{"id":124,"myGeometry":{"type":"Polygon","coordinates":[[[8.7599721,49.7103028],[8.759997,49.7102752],[8.7600145,49.7102818],[8.7600762,49.7102133],[8.760178,49.7102516],[8.7600914,49.7103478],[8.7599721,49.7103028]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":126,"myGeometry":{"type":"LineString","coordinates":[[-69.1991349,-12.6006222],[-69.199136,-12.599842],[-69.1982979,-12.5998268],[-69.1982598,-12.599869],[-69.1982188,-12.5998698],[-69.19817,-12.5998707],[-69.198125,-12.5998218],[-69.1973024,-12.5998133],[-69.1972972,-12.6003109],[-69.197394,-12.6003514],[-69.1973906,-12.6009231],[-69.1975115,-12.601026],[-69.1975081,-12.6010968]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":128,"myGeometry":{"type":"MultiLineString","coordinates":[[[10,10],[20,20],[10,40]],[[40,40],[30,30],[40,20],[30,10]]],"crs":null}}}
{"Geometries":{"id":132,"myGeometry":{"type":"Point","coordinates":[1,2,3,4],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":133,"myGeometry":{"type":"Polygon","coordinates":[[[743238,2967416],[743265.625,2967416],[743265,2967450],[743238,2967450],[743238,2967416]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":134,"myGeometry":{"type":"LineString","coordinates":[[-113.98,39.198],[-113.981,39.195]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":135,"myGeometry":{"type":"LineString","coordinates":[[1,2],[4,5],[7,8]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":136,"myGeometry":{"type":"Polygon","coordinates":[[[10,10],[11,10],[11,11],[10,11],[10,10]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":123,"myGeometry":{"type":"Point","coordinates":[-118.4,33.93],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":127,"myGeometry":{"type":"MultiPoint","coordinates":[[10,40],[40,30],[20,20],[30,10]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":129,"myGeometry":{"type":"MultiPolygon","coordinates":[[[[40,40],[20,45],[45,30],[40,40]]],[[[20,35],[10,30],[10,10],[30,5],[45,20],[20,35]],[[30,20],[20,15],[20,25],[30,20]]]],"crs":null}}}
{"Geometries":{"id":130,"myGeometry":{"type":"Point","coordinates":[-71.1043443253471,42.3150676015829],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":131,"myGeometry":{"type":"Point","coordinates":[1,2,3],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
{"Geometries":{"id":137,"myGeometry":{"type":"Polygon","coordinates":[[[35,10],[45,45],[15,40],[10,20],[35,10]],[[20,30],[35,35],[30,20],[20,30]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}}}
```
Running the query above will list all of the data in your newly created dataset, and it will also show you in some detail more information on the `geometry` objects you have inserted into the dataset. Each `geometry` object consists of:  

* type  
    The type is a string representing the geometric type of the `geometry` object. The type can be:  
    * `Point` / `MultiPoint`
    * `LineString` / `MultiLinestring`
    * `Polygon` / `MultiPolygon`
    * `Geometry Collection`
* coordinates  
    This is a list of the coordinates of the vertices of geometric object. Coordinates may be 2D, 3D, or 4D where the 3rd and 4th dimensions are optional and referred to as Z and M respectively.
* crs  
    CRS stands for coordinate reference system.This is usually assigned automatically as `EPSG:4326` but you can specify the CRS of a `geometry` object on construction as well.


## <a id="query2">Spatial Analysis Functions</a>
 
The following query filters out only the geometries of type `Polygon` and displays the geometry in the Well-Known Text format along with the area of the relevant geometry.
```SQL
USE GISTest;

FROM Geometries AS geo
WHERE geometry_type(geo.myGeometry)='Polygon'
SELECT VALUE {"Polygon":st_as_text(geo.myGeometry), "Area":st_area(geo.myGeometry)};
```
Result:
```JSON
{"Polygon":"POLYGON ((8.7599721 49.7103028, 8.759997 49.7102752, 8.7600145 49.7102818, 8.7600762 49.7102133, 8.760178 49.7102516, 8.7600914 49.7103478, 8.7599721 49.7103028))","Area":1.3755215000294761e-8}
{"Polygon":"POLYGON ((743238 2967416, 743265.625 2967416, 743265 2967450, 743238 2967450, 743238 2967416))","Area":928.625}
{"Polygon":"POLYGON ((10 10, 11 10, 11 11, 10 11, 10 10))","Area":1}
{"Polygon":"POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10), (20 30, 35 35, 30 20, 20 30))","Area":675}
```


<!-- ## <a id="query2">Spatial aggregate example</a>
 
`st_union` function has been implemented both as a normal and an aggregate function. The following query shows how to query the aggregate version of this function:
 
```
USE GISTest;

st_union((SELECT VALUE gbu.myGeometry FROM Geometries as gbu));
```

result:

```
{"type":"MultiPolygon","coordinates":[[[[10,10],[30,5],[35,10],[45,20],[38.90243902439025,23.65853658536585],[41.34146341463415,32.19512195121951],[45,30],[42.27272727272727,35.45454545454545],[45,45],[30,42.5],[20,45],[25.434782608695656,41.73913043478261],[15,40],[12.857142857142858,31.428571428571427],[10,30],[10,20],[10,11],[10,10]],[[32.5,27.5],[25.357142857142858,31.785714285714285],[35,35],[32.5,27.5]],[[20,15],[20,16],[21.11111111111111,15.555555555555555],[20,15]]],[[[8.7600762,49.7102133],[8.760178,49.7102516],[8.7600914,49.7103478],[8.7599721,49.7103028],[8.759997,49.7102752],[8.7600145,49.7102818],[8.7600762,49.7102133]]],[[[743238,2967416],[743265.625,2967416],[743265,2967450],[743238,2967450],[743238,2967416]]]],"crs":null}
``` -->


## <a id="range">Range Query</a>
 
AsterixDB supports doing range scans for geometry objects using a variety of different predicates. For example, the next query finds the geometries that intersects with the given Polygon:
```SQL
USE GISTest;

FROM Geometries geo
WHERE st_intersects(geo.myGeometry, st_geom_from_text("POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))"))
SELECT VALUE geo.myGeometry;
```
Result:
```JSON
{"type":"Point","coordinates":[1,2,3,4],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
{"type":"LineString","coordinates":[[1,2],[4,5],[7,8]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
{"type":"Point","coordinates":[1,2,3],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
```
You can find a full list of the Spatial Predicates in the [GIS Funtions](./functions.html) page.


## <a id="knn">K Nearest Neighbor (KNN) Query</a>

AsterixDB also supports finding the nearest geometries to another given geometry.

AsterixDB does this using an "order by distance" operator that triggers the database to use an index to speed up a sorted return set. Similarly, a nearest neighbor query can be used to efficiently return the "N nearest geometries" just by adding a "limit" statement to the "order by distance" operator.
```SQL
USE GISTest;

FROM Geometries geo
SELECT VALUE geo.myGeometry
ORDER BY st_distance(geo.myGeometry, st_make_point(1,2))
LIMIT 5;
```
The result is:
```JSON
{"type":"Point","coordinates":[1,2,3,4],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
{"type":"LineString","coordinates":[[1,2],[4,5],[7,8]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
{"type":"Point","coordinates":[1,2,3],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
{"type":"Polygon","coordinates":[[[10,10],[11,10],[11,11],[10,11],[10,10]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
{"type":"MultiLineString","coordinates":[[[10,10],[20,20],[10,40]],[[40,40],[30,30],[40,20],[30,10]]],"crs":null}
```

## <a id="joint">Spatial Join Query</a>

Spatial joins allow you to combine information from different `datasets` by using spatial relationships as the join key. Most of the spatial analysis tasks may require a spatial join of some kind. You can find a more comprehensive spatial join example [here](../../resources/data/SJ.sqlpp).

For the spatial join query example, let us create a new dataverse and two new data types:
```SQL
DROP  DATAVERSE SJTest IF EXISTS;
CREATE  DATAVERSE SJTest;
USE SJTest;

CREATE TYPE StateType AS{
  id : int,
  name: string,
  boundary : geometry
};

CREATE DATASET States (StateType) PRIMARY KEY id;

CREATE TYPE POIType AS {
  id : int,
  longitude : double,
  latitude : double
};

CREATE DATASET POIS (POIType) PRIMARY KEY id;
```
Then insert data into states dataset:
```SQL
USE SJTest;

INSERT INTO States ([
  {"id": 1, "name": "Nebraska", "boundary": st_geom_from_text("POLYGON ((-104.05341854507101 41.1705389679833, -104.053028 43.000586999999996, -98.49855 42.99856, -98.01304599999999 42.762299, -97.306677 42.867604, -96.38600699999999 42.474495, -96.06487899999999 41.79623, -96.09200799999999 41.53391, -95.87468899999999 41.307097, -95.88534899999999 40.721092999999996, -95.30829 39.999998, -102.051744 40.003077999999995, -102.051614 41.002376999999996, -104.053249 41.001405999999996, -104.05341854507101 41.1705389679833))") },
  {"id": 2, "name": "Washington", "boundary": st_geom_from_text("MULTIPOLYGON (((-124.732755385025 48.165328947686795, -124.676262 48.391371, -123.981032 48.164761, -123.10189199999999 48.184951999999996, -122.871992 47.993493, -122.75413 48.1447, -122.610341 47.887343, -122.784553 47.686561, -122.864651 47.804669, -123.157948 47.356235999999996, -122.874586 47.413874, -123.119681 47.385532, -122.525329 47.912335999999996, -122.54636949132416 47.317877648507704, -122.324833 47.348521, -122.43694099999999 47.661719, -122.218982 48.020275999999996, -122.383911 48.227486, -122.47892813788141 48.175746487177165, -122.388048 48.30083, -122.57760827271139 48.38291646865838, -122.505828 48.297677, -122.732358 48.226144, -122.3773 47.905941, -122.769939 48.227548, -122.60660653630984 48.395473767832804, -122.674158 48.424726, -122.425271 48.599522, -122.535803 48.776128, -122.673472 48.733081999999996, -122.75802 49.002356999999996, -117.032351 48.999188, -117.062748 46.353623999999996, -116.915989 45.995413, -118.987129 45.999855, -121.145534 45.607886, -121.533106 45.726541, -122.266701 45.543841, -122.67500799999999 45.618038999999996, -123.004233 46.133823, -124.07776799999999 46.272324, -124.06905 46.647258, -123.953699 46.378845, -123.829356 46.713356, -124.092176 46.741623999999995, -124.138225 46.905533999999996, -123.83890000000001 46.953950999999996, -124.122057 47.04165, -124.173877 46.927234999999996, -124.425195 47.738434, -124.732755385025 48.165328947686795), (-122.56199279209496 47.29381043649037, -122.683943 47.365154999999994, -122.76539771783851 47.18116187703539, -122.678476 47.102742, -122.56199279209496 47.29381043649037), (-122.77734484602688 47.19194045282469, -122.82666 47.405806999999996, -122.871472 47.276861, -122.77734484602688 47.19194045282469)), ((-122.4789801236288 48.17567493623048, -122.358963 48.054851, -122.510562 48.132207, -122.4789801236288 48.17567493623048)), ((-122.526031 47.358906, -122.457246 47.505848, -122.373627 47.388718, -122.526031 47.358906)))") },
  {"id": 3, "name": "New Mexico", "boundary": st_geom_from_text("POLYGON ((-109.050173 31.480003999999997, -109.045223 36.999083999999996, -103.002199 37.000104, -103.064423 32.000518, -106.618486 32.000495, -106.528242 31.783147999999997, -108.208394 31.783599, -108.208573 31.333395, -109.050044 31.332501999999998, -109.050173 31.480003999999997))") },
  {"id": 4, "name": "South Dakota", "boundary": st_geom_from_text("POLYGON ((-104.057698 44.997431, -104.045443 45.94531, -96.563672 45.935238999999996, -96.857751 45.605962, -96.45306699999999 45.298114999999996, -96.45326 43.500389999999996, -96.60285999999999 43.450907, -96.436589 43.120841999999996, -96.639704 42.737071, -96.44550799999999 42.490629999999996, -97.23786799999999 42.853139, -98.035034 42.764205, -98.49855 42.99856, -104.053028 43.000586999999996, -104.057698 44.997431))") }
]);
```
Then insert data into POIS dataset:
```SQL
USE SJTest;

INSERT INTO POIS ([
  {"id": 477884092592037888, "latitude": 41.1029498, "longitude": -96.2632202 },
  {"id": 477689754977181696, "latitude": 47.23433434, "longitude": -122.15083003 },
  {"id": 477697263058157569, "latitude": 35.27988499, "longitude": -106.6787443 },
  {"id": 477833117374611456, "latitude": 44.11614436, "longitude": -103.06577797 },
  {"id": 477957785909735424, "latitude": 39.81871193, "longitude": -75.53023171 },
  {"id": 477890178640384001, "latitude": 37.5688636, "longitude": -77.4540628 },
  {"id": 478004308827717632, "latitude": 39.14933024, "longitude": -84.43623134 },
  {"id": 478029048799846401, "latitude": 40.3030824, "longitude": -121.228368 }
]);
```
Now let us perform the spatial join query:
```SQL
USE SJTest;

FROM States, POIS
WHERE st_contains(States.boundary, st_make_point(POIS.longitude, POIS.latitude))
SELECT States.name, POIS.id;
```
And the result is:
```JSON
{ "name": "Nebraska", "id": 477884092592037888 }
{ "name": "Washington", "id": 477689754977181696 }
{ "name": "South Dakota", "id": 477833117374611456 }
{ "name": "New Mexico", "id": 477697263058157569 }
```