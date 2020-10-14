
http://localhost:8080/ors/v2/directions?profile=foot-walking&start=35.022669,-85.124581&end=35.021020,-85.129970

## geocode
curl --include \
     --header "Content-Type: application/json; charset=utf-8" \
     --header "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8" \
  'http://localhost:8080/ors/geocode/reverse?api_key=5b3ce3597851110001cf62483dc901898fb1483684ef9393a933c433&point.lon=35.022687&point.lat=-85.124602'

## directions
curl --include \
     --header "Content-Type: application/json; charset=utf-8" \
     --header "Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8" \
  'http://localhost:8080/ors/v2/directions/driving-car?api_key=5b3ce3597851110001cf62483dc901898fb1483684ef9393a933c433&start=-74.017744,40.706075&end=-74.017362,40.704916'


## isochrones
 curl -X POST \
  'http://localhost:8080/ors/v2/isochrones/driving-car' \
  -H 'Content-Type: application/json; charset=utf-8' \
  -H 'Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' \
  -H 'Authorization: 5b3ce3597851110001cf62483dc901898fb1483684ef9393a933c433' \
  -d '{"locations":[[-74.017744,40.706075],[-74.017362,40.704916]],"range":[300,200]}'
