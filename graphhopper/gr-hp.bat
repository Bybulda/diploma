@echo off
java ^
 -Ddw.graphhopper.datareader.file=data/map.osm.pbf ^
 -Ddw.graphhopper.graph.flag_encoders=car ^
 -Ddw.graphhopper.ch.disable=true ^
 -jar web\target\graphhopper-web-9.0-SNAPSHOT.jar ^
 server config-example.yml

pause
