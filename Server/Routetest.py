from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint
import json
from breakuproute import break_up_route
# create an instance of the API class
api_instance = swagger_client.RoutingApi()
point = ['51.51456185,-0.03501226201922958','53.4794892,-2.2451148'] # list[str] | Specify multiple points for which the route should be calculated. The order is important. Specify at least two points.
points_encoded = False # bool | IMPORTANT- TODO - currently you have to pass false for the swagger client - Have not found a way to force add a parameter. If `false` the coordinates in `point` and `snapped_waypoints` are returned as array using the order [lon,lat,elevation] for every point. If `true` the coordinates will be encoded as string leading to less bandwith usage. You'll need a special handling for the decoding of this string on the client-side. We provide open source code in [Java](https://github.com/graphhopper/graphhopper/blob/d70b63660ac5200b03c38ba3406b8f93976628a6/web/src/main/java/com/graphhopper/http/WebHelper.java#L43) and [JavaScript](https://github.com/graphhopper/graphhopper/blob/d70b63660ac5200b03c38ba3406b8f93976628a6/web/src/main/webapp/js/ghrequest.js#L139). It is especially important to use no 3rd party client if you set `elevation=true`!
key = 'c9233b36-a5f6-4e05-a0e7-f1f8447f1cbb' # str | Get your key at graphhopper.com
locale = 'locale_example' # str | The locale of the resulting turn instructions. E.g. `pt_PT` for Portuguese or `de` for German (optional)
instructions = True # bool | If instruction should be calculated and returned (optional)
vehicle = 'car' # str | The vehicle for which the route should be calculated. Other vehicles are foot, small_truck, ... (optional)
elevation = False # bool | If `true` a third dimension - the elevation - is included in the polyline or in the GeoJson. If enabled you have to use a modified version of the decoding method or set points_encoded to `false`. See the points_encoded attribute for more details. Additionally a request can fail if the vehicle does not support elevation. See the features object for every vehicle. (optional)
calc_points = True # bool | If the points for the route should be calculated at all printing out only distance and time. (optional)
point_hint = [] # list[str] | Optional parameter. Specifies a hint for each `point` parameter to prefer a certain street for the closest location lookup. E.g. if there is an address or house with two or more neighboring streets you can control for which street the closest location is looked up. (optional)
ch_disable = True # bool | Use this parameter in combination with one or more parameters of this table (optional)
weighting = 'shortest' # str | Which kind of 'best' route calculation you need. Other option is `shortest` (e.g. for `vehicle=foot` or `bike`), `short_fastest` if time and distance is expensive e.g. for `vehicle=truck` (optional)
edge_traversal = False # bool | Use `true` if you want to consider turn restrictions for bike and motor vehicles. Keep in mind that the response time is roughly 2 times slower. (optional)
algorithm = 'alternative_route' # str | The algorithm to calculate the route. Other options are `dijkstra`, `astar`, `astarbi`, `alternative_route` and `round_trip` (optional)
heading = 56 # int | Favour a heading direction for a certain point. Specify either one heading for the start point or as many as there are points. In this case headings are associated by their order to the specific points. Headings are given as north based clockwise angle between 0 and 360 degree. This parameter also influences the tour generated with `algorithm=round_trip` and force the initial direction. (optional)
heading_penalty = 56 # int | Penalty for omitting a specified heading. The penalty corresponds to the accepted time delay in seconds in comparison to the route without a heading. (optional)
pass_through = True # bool | If `true` u-turns are avoided at via-points with regard to the `heading_penalty`. (optional)
details = ['details_example'] # list[str] | List of additional trip attributes to be returned. Try some of the following: `average_speed`, `street_name`, `edge_id`, `time`, `distance`. (optional)
round_trip_distance = 56 # int | If `algorithm=round_trip` this parameter configures approximative length of the resulting round trip (optional)
round_trip_seed = 789 # int | If `algorithm=round_trip` this parameter introduces randomness if e.g. the first try wasn't good. (optional)
alternative_route_max_paths = 56 # int | If `algorithm=alternative_route` this parameter sets the number of maximum paths which should be calculated. Increasing can lead to worse alternatives. (optional)
alternative_route_max_weight_factor = 56 # int | If `algorithm=alternative_route` this parameter sets the factor by which the alternatives routes can be longer than the optimal route. Increasing can lead to worse alternatives. (optional)
alternative_route_max_share_factor = 56 # int | If `algorithm=alternative_route` this parameter specifies how much alternatives routes can have maximum in common with the optimal route. Increasing can lead to worse alternatives. (optional)
avoid = 'avoid_example' # str | comma separate list to avoid certain roads. You can avoid motorway, ferry, tunnel or track (optional)
maxtraveltime=3600000 #currently one hour should probably change
def makeroute(point,key,maxtraveltime,points_encoded=False):
	coordinates = []
	try:
	    # Routing Request
	    #api_response = api_instance.route_get(point, points_encoded, key, locale=locale, instructions=instructions, vehicle=vehicle, elevation=elevation, calc_points=calc_points, point_hint=point_hint, ch_disable=ch_disable, weighting=weighting, edge_traversal=edge_traversal, algorithm=algorithm, heading=heading, heading_penalty=heading_penalty, pass_through=pass_through, details=details, round_trip_distance=round_trip_distance, round_trip_seed=round_trip_seed, alternative_route_max_paths=alternative_route_max_paths, alternative_route_max_weight_factor=alternative_route_max_weight_factor, alternative_route_max_share_factor=alternative_route_max_share_factor, avoid=avoid)
	    api_response = api_instance.route_get(point, points_encoded, key)
	    #pprint(api_response)
	    coordinates.append(point[0])
	    coordinates += break_up_route(api_response.to_dict(),maxtraveltime)
	    coordinates.append(point[-1])
	    #print(coordinates)
	    for i in range(0,len(coordinates)-1):
	    	api_response2 = api_instance.route_get([coordinates[i],coordinates[i+1]],points_encoded,key)
	    	f = open('route'+str(i)+'.txt','w+')
	    	f.write(json.dumps(api_response2.to_dict()))
	    	f.close()

	except ApiException as e:
	    print("Exception when calling RoutingApi->route_get: %s\n" % e)
makeroute(point,key,maxtraveltime)



