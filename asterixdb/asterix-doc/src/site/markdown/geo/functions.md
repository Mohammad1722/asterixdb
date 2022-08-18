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

# Support the standard GIS objects (DRAFT) #
## <a id="toc">Table of Contents</a> ##

* [Introduction](#Introduction)
* [Construction functions](#construction)
* [Primitive functions](#primitive)
* [Spatial Predicate](#predicate)
* [Spatial Analysis](#analysis)
* [Spatial Aggregates](#aggregate)

## <a id="Introduction">Introduction</a>
 
To support standard GIS objects in AsterixDB, you need to use the `geometry` data type as follows.

```SQL
DROP dataverse GeoJSON if exists;
CREATE  dataverse GeoJSON;

USE GeoJSON;

CREATE TYPE GeometryType AS{
  id : int,
  myGeometry : geometry
};

CREATE DATASET Geometries (GeometryType) PRIMARY KEY id;
```

Please note that even though the [SRID](http://desktop.arcgis.com/en/arcmap/10.3/manage-data/using-sql-with-gdbs/what-is-an-srid.htm)
input is supported for certain functions and is represented internally in the correct manner the serialized result (printed in the output) displays the SRID as 4326 always because of the limitations in Esri API.

## <a id="construction">Construction functions</a>

### st_make_point ###

* Syntax:
    ```SQL
    st_make_point(numeric_value1, numeric_value2[, numeric_value3[, numeric_value4]]);
    ```
* Creates a 2D, 3D or 4D point geometry.
* Arguments:
    * `numeric_value1`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` value for the X-coordinate,
    * `numeric_value2`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` value for the Y-coordinate,
    * `numeric_value3`: (optional) a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` value for the Z-coordinate,
    * `numeric_value4`: (optional) a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` value for the M-coordinate.
* Return Value:
    * A Point representing the given x,y,z and m coordinates.
* Example:
    ```SQL
    st_make_point(-71, 42, 15.66);
    ```
* The expected result is:
    ```JSON
    {"type":"Point","coordinates":[-71,42,15.66],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_geom_from_text ###
* Syntax:
    ```SQL
    st_geom_from_text(wkt_string);
    ```
* Creates a specified ST_Geometry value from Well-Known Text representation (WKT).
* Arguments:
    * `wkt_string`: a WKT-compliant `string` value representing a geometric object.
* Return Value:
    * A `Point`/`LineString`/`Polygon`/`MultiPoint`/`MultiLineString`/`MultiPolygon`/`GeometryCollection` geometry object corresponding to the `wkt_string`.
* Example:
    ```SQL
    st_geom_from_text("LINESTRING(1 2,3 4,5 6)");
    ```
* The expected result is:
    ```JSON
    {"type":"LineString","coordinates":[[1,2],[3,4],[5,6]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```
* Example:
    ```SQL
    st_geom_from_text('MULTILINESTRING((1 2,3 4,5 6),(7 8,9 10))');
    ```
* The expected result is:
    ```JSON
    {"type":"MultiLineString","coordinates":[[[1,2],[3,4],[5,6]],[[7,8],[9,10]]],"crs":null}
    ```


### st_geom_from_wkb ###
* Syntax:
    ```SQL
    st_geom_from_wkb(wkb_binary);
    ```
* Creates a geometry instance from a Well-Known Binary geometry representation (WKB) and optional SRID.
* Arguments:
    * `wkb_binary`: a WKB-compliant `binary` value representing a geometric object.
* Return Value:
    * A `Point`/`LineString`/`Polygon`/`MultiPoint`/`MultiLineString`/`MultiPolygon`/`GeometryCollection` geometry object corresponding to the `wkb_string`.
* Example:
    ```SQL
    st_geom_from_wkb(hex("010100000000000000000000400000000000001440"));
    ```
* The expected result is:
    ```JSON
    {"type":"Point","coordinates":[2,5],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```
     

### st_geom_from_geojson ###
* Syntax:
    ```SQL
    st_geom_from_geojson(geojson_object)
    ```
* Creates a geometry stance from its GeoJSON geometries representation.
* Arguments:
    * `geojson_object`: a GeoJSON-compliant `object` representing a geometric object.
* Return Value:
    * A `Point`/`LineString`/`Polygon`/`MultiPoint`/`MultiLineString`/`MultiPolygon`/`GeometryCollection` geometry object corresponding to the `geojson_object`.
* Example:
    ```SQL
    st_geom_from_geojson({"type":"Point","coordinates":[2,5],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}});
    ```
* The expected result is:
    ```JSON
    {"type":"Point","coordinates":[2,5],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```

### st_make_envelope ###
* Syntax:
    ```SQL
    st_make_envelope(x_min, y_min, x_max, x_max, srid);
    ```
* Creates a rectangul Polygon formed from the given minima and maxima. Input values must be in SRS specified by the SRID.
* Arguments:
    * `x_min`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` representing the minimum x-value for the region.
    * `y_min`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` representing the minimum y-value for the region.
    * `x_max`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` representing the maximum x-value for the region.
    * `x_max`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` representing the maximum y-value for the region.
    * `srid`: a `tinyint`/`smallint`/`integer`/`bigint`/`float`/`double` representing the SRID of the SRS.
* Return Value:
    * A `Polygon` geometry object corresponding to the given minima and maxima.
* Example:
    ```SQL
    st_make_envelope(10, 30, 20, 40, 4326);
    ```
* The expected result is:
    ```JSON
    {"type":"Polygon","coordinates":[[[10,30],[20,30],[20,40],[10,40],[10,30]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


## <a id="primitive">Primitive functions</a>

### st_area ###
* Syntax:
    ```SQL
    st_area(geom_polygon);
    ```
* Calculates the area of a `Polygon` or `MultiPolygon`. For geometry, a 2D Cartesian area is determined with units specified by the SRID.
* Arguments:
    * `geom_polygon`: a `geometry` object of type `Polygon`/`MultiPolygon`/`GeometryCollection` to calculate the area of its surface.
* Return Value:
    * a `double` value;
        * The area of the surface of the argument if it is a `Polygon`/`MultiPolygon`.
        * The area of the surface of the first `Polygon`/`MultiPolygon` in the `GeometryCollection` if the argument is a `GeometryCollection`.
        * 0 for other `geometry` objects.
* Example:
    ```SQL
    st_area(st_geom_from_text('POLYGON((7 2,4 9,3 6,2.6 7,8 16))'));
    ```
* The expected result is:
    ```JSON
    26.500000000000007
    ```


### st_coord_dim ###
* Syntax:
    ```SQL
    st_coord_dim(geom_object)
    ```
* Returns the coordinate dimension of the `geom_object`.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * an `integer` value representing the coordinate dimension.
* Example:
    ```SQL
    st_coord_dim(st_make_point(1,2));
    ```
* The expected result is:
    ```JSON
    2
    ```


### st_dimension ###
* Syntax:
    ```SQL
    st_dimension(geom_object);
    ```
* Returns the inherent dimension of this `geom_object`, which must be less than or equal to the coordinate dimension.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * an `integer` value representing the inherent dimension.
* Example:
    ```SQL
    st_dimension(st_geom_from_text('GEOMETRYCOLLECTION(LINESTRING(1 1,0 0),POINT(0 0))'));
    ```
* The expected result is:
    ```JSON
    1
    ```


### geometry_type ###
* Syntax:
    ```SQL
    geometry_type(geom_object)
    ```
* Returns the type of `geom_object` as a string.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `string` value representing the geometry type.
        * `Point`
        * `Polygon`
        * etc.
* Example:
    ```SQL
    geometry_type(st_geom_from_text('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'));
    ```
* The expected result is:
    ```JSON
    "LineString"
    ```


### st_n_points ###
* Syntax:
    ```SQL
    st_n_points(geom_object)
    ```
* Returns the number of points vertices in a `geometry` object.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * an `integer` representing the vertices count.
* Example:
    ```SQL
    st_n_points(st_geom_from_text('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'));
    ```
* The expected result is:
    ```JSON
    4
    ```


### st_n_rings ###
* Syntax:
    ```SQL
    st_n_rings(geom_polygon)
    ```
* Returns the number of rings in `geom_polygon`.
* Arguments:
    * `geom_polygon`: a `geometry` object of type `Polygon`/`MultiPolygon`.
* Return Value:
    * an `integer` representing the ring count.
* Example:
    ```SQL
    st_n_rings(st_geom_from_text('POLYGON((10.689 -25.092, 34.595 -20.170, 38.814 -35.639, 13.502 -39.155, 10.689 -25.092))'));
    ```
* The expected result is:
    ```JSON
    1
    ```

### st_num_geometries ###
* Syntax:
    ```SQL
    st_num_geometries(geom_object)
    ```
* Returns the number of geometries in a geometry collection.
* Arguments:
    * `geom_object`: a `geometry` object of any type.
* Return Value:
    * an `integer` representing the geometry count if the input is a `GeometryCollection`/`MultiPoint`/`MultiPolygon`,
    * `1` if the input is a single geometry,
    * `NULL` otherwise.
* Example:
    ```SQL
    st_num_geometries(st_geom_from_text('GEOMETRYCOLLECTION(MULTIPOINT(-2 3 , -2 2), LINESTRING(5 5 ,10 10), POLYGON((-7 4.2,-7.1 5,-7.1 4.3,-7 4.2)))'));
    ```
* The expected result is:
    ```JSON
    3
    ```
* Example:
    ```SQL
    st_num_geometries(st_geom_from_text('LINESTRING(77.29 29.07,77.42 29.26,77.27 29.31,77.29 29.07)'));
    ```
* The expected result is:
    ```JSON
    1
    ```


### st_num_interiorRings ###
* Syntax:
    ```SQL
    st_num_interiorRings(geom_polygon)
    ```
* Returns the number of interior rings of `geom_polygon`.
* Arguments:
    * `geom_polygon`: a `geometry` object of type `Polygon`.
* Return Value:
    * an `integer` representing the interior rings count.
* Example:  
    ![Image of interiorRings](../images/linestring.png)
    ```SQL
    st_num_interior_rings(st_geom_from_text("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))"));
    ```
* The expected result is:
    ```JSON
    1
    ```


### st_x/st_y/st_z/st_m ###
* Syntax:
    ```SQL
    st_x/st_y/st_z/st_m(geom_point)
    ```
* Returns the X/Y/Z/M coordinate of `geom_point`, or `NULL` if not available. Input must be a point.
* Arguments:
    * `geom_point`: a `geometry` object of type `Point`.
* Return Value:
    * a `double` representing the X/Y/Z/M coordinate.
    * `NULL` if the coordinate doesn't exist. (e.g., finding the M-coordinate of a 2D point)
        > Note: st_make_point throws an error for missing/null X or Y values.
* Example:
    ```SQL
    st_x(st_make_point(1, 2, 3, 4));
    ```
* The expected result is:
    ```JSON
    1
    ```


### st_x_max/st_y_max/st_z_max ###
* Syntax:
    ```SQL
    st_x_max/st_y_max/st_z_max(geom_object)
    ```
* Returns the maximum X/Y/Z coordinate of `geom_object`.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `double` representing the maximum X/Y/Z coordinate.
* Example:
    ```SQL
    st_x_max(st_geom_from_text('POLYGON((10.689 -25.092, 34.595 -20.170, 38.814 -35.639, 13.502 -39.155, 10.689 -25.092))'));
    ```
* The expected result is:
    ```JSON
    38.814
    ```


### st_x_min/st_y_min/st_z_min ###
* Syntax:
    ```SQL
    st_x_min/st_y_min/st_z_min(geom_object)
    ```
* Returns the minimum X/Y/Z coordinate of `geom_object`.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `double` representing the minimum X/Y/Z coordinate.
* Example:
    ```SQL
    st_x_min(st_geom_from_text('POLYGON((10.689 -25.092, 34.595 -20.170, 38.814 -35.639, 13.502 -39.155, 10.689 -25.092))'));
    ```
* The expected result is:
    ```JSON
    10.689
    ```


### st_as_binary ###
* Syntax:
    ```SQL
    st_as_binary(geom_object)
    ```
* Returns the Well-Known Binary (WKB) representation of the `geometry`.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `binary` value for the WKB representation.
* Example:
    ```SQL
    st_as_binary(st_geom_from_geojson({"type":"Point","coordinates":[2,5],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}));
    ```
* The expected result is:
    ```JSON
    "010100000000000000000000400000000000001440"
    ```


### st_as_geojson ###
* Syntax:
    ```SQL
    st_as_geojson(geom_object)
    ```
* Returns the GeoJSON representation of a `geometry` object.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `string` value for the GeoJSON representation.
* Example:
    ```SQL
    st_as_geojson(st_geom_from_text('POLYGON((10.689 -25.092, 34.595 -20.170, 38.814 -35.639, 13.502 -39.155, 10.689 -25.092))'));
    ```
* The expected result is:
    ```JSON
    "{\"type\":\"Polygon\",\"coordinates\":[[[10.689,-25.092],[13.502,-39.155],[38.814,-35.639],[34.595,-20.17],[10.689,-25.092]]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}"
    ```


### st_distance ###
* Syntax:
    ```SQL
    st_distance(g1, g2)
    ```
* For `geometry` type returns the 2D Cartesian distance between two geometries in projected units (based on the spatial reference).
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Type:
    * `double`

* Example:
    ```SQL
    st_distance(st_geom_from_text('POINT(-72.1235 42.3521)'),st_geom_from_text('LINESTRING(-72.1260 42.45, -72.123 42.1546)'));
    ```
* The expected result is:
    ```JSON
    0.0015056772638282166
    ```


### st_length ###
* Syntax:
    ```SQL
    st_length(geom_line)
    ```
* Returns the 2D length of the `geom_line` if it is a `LineString`/`MultiLineString` in units of spatial reference.
* Arguments:
    * `geom_line`: a `geometry` object of type `LineString`/`MultiLineString`.
* Return:
    * `double`
* Example:
    ```SQL
    st_length(st_geom_from_text('LINESTRING(-72.1260 42.45, -72.1240 42.45666, -72.123 42.1546)'));
    ```
* The expected result is:
    ```JSON
    0.30901547439030225
    ```


## <a id="predicate">Spatial Predicate</a>

### st_intersects ###
* Syntax:
    ```SQL
    st_intersects(g1, g2)
    ```
* Returns `true` if `g1` and `g2` spatially intersect in 2D.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if objects intersect,
    * `false` otherwise.
* Example:
    ```SQL
    st_intersects(st_geom_from_text('POINT(0 0)'), st_geom_from_text('LINESTRING ( 0 0, 0 2 )'));
    ```
* The expected result is:
    ```JSON
    true
    ```

### st_isclosed ###
* Syntax:
    ```SQL
    st_isclosed(`geom_line`)
    ```
* Return `true` if the `LineString`'s start and end points are coincident.
* Arguments:
    * `geom_line`: a `geometry` object of type `LineString`/`MultiLineString`.
* Return Value:
    * `true` if the line is closed or the `geometry` object is not  a `LineString`/`MultiLineString`,
    * `false` otherwise.
* Example:
    ```SQL
    st_is_closed(st_geom_from_text('LINESTRING(0 0, 0 1, 1 1, 0 0)'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_iscollection ###
* Syntax:
    ```SQL
    st_iscollection(geom_object)
    ```
* Returns `true` if the argument is a collection (`Multi*`, `GeometryCollection`, ...)
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * `true` if the object is of type `GeometryCollection`/`MultiPoint`/`MultiPolygon`.
    * `false` otherwise.
* Example:
    ```SQL
    st_is_collection(st_geom_from_text('MULTIPOINT EMPTY'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_is_empty ###
* Syntax:
    ```SQL
    geom_collection(geom_object)
    ```
* Returns true if `geom_object` is an empty collection (`Multi*`, `GeometryCollection`, ...).
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * `true` if the object is of type `GeometryCollection`/`MultiPoint`/`MultiPolygon` and empty.
    * `false` otherwise.
* Example:
    ```SQL
    st_is_empty(st_geom_from_text('POLYGON EMPTY'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_is_ring ###
* Syntax:
    ```SQL
    st_is_ring(geom_linestring)
    ```
* Return true if the `LineString` is both closed and simple.
* Arguments:
    * `geom_linestring`: a `geometry` object of type `LineString`.
* Return Value:
    * `true` if the `LineString` is both closed and simple.
    * `false` otherwise.
* Example:
    ```SQL
    st_is_ring(st_geom_from_text('LINESTRING(0 0, 0 1, 1 1, 1 0, 0 0)'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_is_simple ###
* Syntax:
    ```SQL
    st_is_simple(geom_object)
    ```
* Returns `true` if `geom_object` has no anomalous geometric points, such as self intersection or self tangency.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * `ture` if `geom_object` has no anomalous geometric points,
    * `false` otherwise.
* Example:
    ```SQL
    st_is_simple(st_geom_from_text('LINESTRING(1 1,2 2,2 3.5,1 3,1 2,2 1)'));
    ```
* The expected result is:
    ```JSON
    false
    ```


### st_contains ###
* Syntax:
    ```SQL
    st_contains(g1, g2)
    ```
* Returns `true` if and only if no points of `g2` lie in the exterior of `g1`, and at least one point of the interior of `g2` lies in the interior of `g1`.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g2` is within `g1`,
    * `false` otherwise.
* Example:
    ```SQL
    st_contains(st_geom_from_text('LINESTRING(1 1,-1 -1,2 3.5,1 3,1 2,2 1)'), st_make_point(-1, -1));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_crosses ###
* Syntax:
    ```SQL
    st_crosses(g1, g2)
    ```
* Returns `true` if the applied geometries have some, but not all, interior points in common.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g2` crosses `g1`,
    * `false` otherwise.
* Example:
    ```SQL
    st_crosses(st_geom_from_text('LINESTRING(1 1,2 2,3 3,4 4, 5 5,6 6)'), st_geom_from_text('LINESTRING(0 2,1 2,2 2,3 2,4 2,5 2)'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_disjoint ###
* Syntax:
    ```SQL
    st_disjoint(g1, g2)
    ```
* Returns `true` if the geometries do not "spatially intersect"; they do not share any space together. 
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g1` and `g2` are disjoint,
    * `false` otherwise.
* Example:
    ```SQL
    st_disjoint(st_geom_from_text('LINESTRING(1 1,2 2,3 3,4 4, 5 5,6 6)'), st_geom_from_text('POINT(0 0)'));
    ```
* The expected result is:
```JSON
true
```


### st_equals ###
* Syntax:
    ```SQL
    st_equals(g1, g2)
    ```
* Returns `true` if the given geometries represent the same geometry. Directionality is ignored.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g2` equals `g1`,
    * `false` otherwise.
* Example:
    ```SQL
    st_equals(st_geom_from_text('LINESTRING(0 0, 10 10)'), st_geom_from_text('LINESTRING(0 0, 5 5, 10 10)'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_overlaps ###
* Syntax:
    ```SQL
    st_overlaps(g1, g2)
    ```
* Returns `true` if the geometries share space; they are of the same dimension, but are not completely contained by each other.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g1` and `g2` are overlapping,
    * `false` otherwise.
* Example:
    ```SQL
    st_overlaps(st_geom_from_text('LINESTRING(1 1,2 2,3 3,4 4, 5 5,6 6)'), st_geom_from_text('LINESTRING(0 2,1 2,2 2,3 3,4 2,5 2)'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_relate ###
* Syntax:
    ```SQL
    st_relate(g1, g2)
    ```
* Returns `true` if `g1` is spatially related to `g2`, by testing for intersections between the Interior, Boundary and Exterior of the two geometries as specified by the values in the intersection matrix pattern.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g1` is spatially related to `g2`,
    * `false` otherwise.
* Example:
    ```SQL
    st_relate(st_geom_from_text('LINESTRING(1 2, 3 4)'), st_geom_from_text('LINESTRING(5 6, 7 8)'), "FF1FF0102");
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_touches ###
* Syntax:
    ```SQL
    st_touches(g1, g2)
    ```
* Returns `true` if the geometries have at least one point in common, but their interiors do not intersect.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g1` touches `g2`,
    * `false` otherwise.
* Example:
    ```SQL
    st_touches(st_geom_from_text('LINESTRING(0 0, 1 1, 0 2)'), st_geom_from_text('POINT(0 2)'));
    ```
* The expected result is:
    ```JSON
    true
    ```


### st_within ###
* Syntax:
    ```SQL
    st_within(g1, g2)
    ```
* Returns `true` if `g1` is completely inside `g2`.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * `true` if `g1` is within `g2`,
    * `false` otherwise.
* Example
    ```SQL
    st_within(st_geom_from_text('POINT (30 30)'), st_geom_from_text('POLYGON ((10 10, 40 10, 40 40, 10 40, 10 10))'))
    ```
* The expected result is:
    ```JSON
    true
    ```


## <a id="analysis">Spatial Analysis</a>
Spatial analysis functions take as input one or more geometries and return a geometry as output.

### st_union ###
* Syntax:
    ```SQL
    st_union(g1, g2)
    ```
* Returns a `geometry` object that represents the point set union of the Geometries.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * a `geometry` object representing the union.
* Example:
    ```SQL
    st_union(st_geom_from_text('LINESTRING(0 0, 1 1, 0 2)'), st_geom_from_text('POINT(0 2)'));
    ```
* The expected result is:
    ```JSON
    {"type":"LineString","coordinates":[[0,0],[1,1],[0,2]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_boundary ###
* Syntax:
    ```SQL
    st_boundary(geom_object)
    ```
* Returns the closure; the combinatorial boundary of `geom_object`.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `geometry` object representing the union.
* Example:
    ```SQL
    st_boundary(st_geom_from_text('POLYGON((1 1,0 0, -1 1, 1 1))'));
    ```
* The expected result is:
    ```JSON
    {"type":"MultiLineString","coordinates":[[[1,1],[-1,1],[0,0],[1,1]]],"crs":null}
    ```


### st_end_point ###
* Syntax:
    ```SQL
    st_end_point(geom_line)
    ```
* Returns the last point of a `LineString` or `CircularLineString` as a `Point`.
* Arguments:
    * `geom_line`: a `geometry` object of type `LineString` or `CircularLineString`.
* Return Value:
    * a `geometry` object of type `Point` representing the end point.
* Example:
    ```SQL
    st_end_point(st_geom_from_text('LINESTRING(1 1, 2 2, 3 3)'));
    ```
* The expected result is:
    ```JSON
    {"type":"Point","coordinates":[3,3],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_envelope ###
* Syntax:
    ```SQL
    st_envelope(geom_object)
    ```
* Returns a `geometry` representing the double precision (float8) bounding box of the supplied geometry.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `geometry` object representing the bounding box.
* Example:
    ```SQL
    st_envelope(st_geom_from_text('LINESTRING(1 1, 2 2, 3 3)'));
    ```
* The expected result is:
    ```JSON
    {"type":"Polygon","coordinates":[[[1,1],[3,1],[3,3],[1,3],[1,1]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_exterior_ring ###
* Syntax:
    ```SQL
    st_exterior_ring(geom_object)
    ```
* Returns a `LineString` representing the exterior ring of the `Polygon` geometry. Returns `NULL` if `geom_object` is not a `Polygon`. Will not work with `MultiPolygon`.
* Arguments:
    * `geom_object`: a `geometry` object.
* Return Value:
    * a `geometry` object of type `LineString` representing the exterior ring.
    * `NULL` otherwise.
* Example:
    ```SQL
    st_exterior_ring(st_geom_from_text("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))"));
    ```
* The expected result is:
    ```JSON
    {"type":"LineString","coordinates":[[35,10],[45,45],[15,40],[10,20],[35,10]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_geometry_n ###
* Syntax:
    ```SQL
    st_geometry_n(geom_collection, n)
    ```
* Returns the 1-based `n`th `geometry` if `geom_collection` is a `GeometryCollection`/`MultiPoint`/`MultiLineString`/`MultiCurve`/`MultiPolygon`/`PolyhedralSurface`. Otherwise, returns `NULL`.
* Arguments:
    * `geom_collection`: a `geometry` object of type `GeometryCollection`. 
* Return Value:
    * a `geometry` object.
* Example:
    ```SQL
    st_geometry_n(st_geom_from_text('GEOMETRYCOLLECTION(MULTIPOINT(-2 3 , -2 2),LINESTRING(5 5 ,10 10),POLYGON((-7 4.2,-7.1 5,-7.1 4.3,-7 4.2)))'),2);
    ```
* The expected result is:
    ```JSON
    {"type":"Polygon","coordinates":[[[-7,4.2],[-7.1,5],[-7.1,4.3],[-7,4.2]]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_interior_ring_n ###
* Syntax:
    ```SQL
    st_interior_ring_n(geom_polygon, n)
    ```
* __Move description here__
* Returns the Nth interior `LineString` ring of the `Polygon` geometry. Returns `NULL` if the geometry is not a `Polygon` or the given N is out of range.
* Arguments:
    * `geom_polygon`: a `geometry` object of type `Polygon`.
* Return Value:
    * a `geometry` object of type `LineString`.
* Example:
    ```SQL
    st_interior_ring_n(st_geom_from_text("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))"), 0);
    ```
* The expected result is:
    ```JSON
    {"type":"LineString","coordinates":[[20,30],[35,35],[30,20],[20,30]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_point_n ###
* Syntax:
    ```SQL
    st_point_n(geom_line, n)
    ```
* Returns the Nth `Point` in the first `LineString` or `CircularLineString` in the geometry. Negative values are counted backwards from the end of the `LineString`. Returns `NULL` if there is no `LineString` in the geometry.
* Arguments:
    * `geom_line`: a `geometry` object of type `LineString`.
* Return Value:
    * a `geometry` object of type `Point`.
* Example:
    ```SQL
    st_point_n(st_geom_from_text("LINESTRING(1 1, 2 2, 3 3)"), 1);
    ```
* The expected result is:
    ```JSON
    {"type":"Point","coordinates":[2,2],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_start_point ###
* Syntax:
    ```SQL
    st_start_point(geom_line)
    ```
* Returns the first `Point` of a `LineString` geometry as a `Point`.
* Arguments:
    * `geom_line`: a `geometry` object of type `LineString`.
* Return Value:
    * a `geometry` object of type `Point`.
* Example:
    ```SQL
    st_start_point(st_geom_from_text("LINESTRING(1 1, 2 2, 3 3)"));
    ```
* The expected result is:
    ```JSON
    {"type":"Point","coordinates":[1,1],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_difference ###
* Syntax:
    ```SQL
    st_difference(g1, g2)
    ```
* Returns a `geometry` that represents that part of `g1` that does not intersect with `g2`.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * a `geometry` object representing the difference.
* Example:
    ```SQL
    st_difference(st_geom_from_text("LINESTRING(1 1, 2 2, 3 3)"), st_geom_from_text("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))"));
    ```
* The expected result is:
    ```JSON
    {"type":"LineString","coordinates":[[1,1],[2,2],[3,3]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_intersection ###
* Syntax:
    ```SQL
    st_intersection(g1, g2)
    ```
* Returns a geometry that represents the shared portion of `g1` and `g2`.
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * a `geometry` object representing the intersection.
* Example:
    ```SQL
    st_intersection(st_geom_from_text("LINESTRING(1 1,2 2,3 3,4 4, 5 5,6 6)"), st_geom_from_text("LINESTRING(0 2,1 2,2 2,3 3,4 2,5 2)"));
    ```
* The expected result is:
    ```JSON
    {"type":"LineString","coordinates":[[2,2],[3,3]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```


### st_sym_difference ###
* Syntax:
    ```SQL
    st_sym_difference(g1, g2)
    ```
* Returns a geometry that represents the portions of `g1` and `g2` that do not intersect. It is called a symmetric difference because st_sym_difference(`g1`,`g2`) = st_sym_difference(B,`g1`).
* Arguments:
    * `g1`: a `geometry` object.
    * `g2`: a `geometry` object.
* Return Value:
    * a `geometry` object representing the symmetric difference.
* Example:
    ```SQL
    st_sym_difference(st_geom_from_text("LINESTRING(1 1,2 2,3 3,4 4, 5 5,6 6)"), st_geom_from_text("LINESTRING(0 2,1 2,2 2,3 3,4 2,5 2)"));
    ```
* The expected result is:
    ```JSON
    {"type":"MultiLineString","coordinates":[[[0,2],[1,2],[2,2],[1,1]],[[5,2],[4,2],[3,3],[4,4],[5,5],[6,6]]],"crs":null}
    ```


### st_polygonize ###
* Syntax:
    ```SQL
    st_polygonize(geom_set)
    ```
* Aggregate. Creates a `GeometryCollection` containing possible polygons formed from the constituent linework of a set of geometries.
* Arguments:
    * `geom_set`: a set of `geometry` objects.
* Return Value:
    * a `GeometryCollection`.
* Example:
    ```SQL
    st_polygonize([st_geom_from_text("LINESTRING(1 1,2 2,3 3,4 4, 5 5,6 6)"), st_geom_from_text("LINESTRING(0 2,1 2,2 2,3 3,4 2,5 2)")]);
    ```
* The expected result is:
    ```JSON
    {"type":"GeometryCollection","geometries":[{"type":"LineString","coordinates":[[1,1],[2,2],[3,3],[4,4],[5,5],[6,6]]},{"type":"LineString","coordinates":[[0,2],[1,2],[2,2],[3,3],[4,2],[5,2]]}],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
    ```

<!-- ## <a id="aggregate">Spatial Aggregates</a>
spatial aggregate function which takes as input a set of geometries and return one geometry as the result.

### st_union ###
* Syntax:
    ```SQL
    SYNTAX
    ```
* Returns a geometry at represents the point set union of the Geometries.
* Arguments:
    * `arg1`: arg1 description
* Return Value:
    * possible return value
    * another possible return value


* Example:
```SQL
st_union((SELECT VALUE gbu FROM [st_make_point(1.0,1.0),st_make_point(1.0,2.0)] as gbu));
```
* The expected result is:
```JSON
{"type":"MultiPoint","coordinates":[[1,1],[1,2]],"crs":{"type":"name","properties":{"name":"EPSG:4326"}}}
``` -->