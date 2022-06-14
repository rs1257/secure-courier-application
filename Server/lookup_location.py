from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint
# create an instance of the API class
api_instance = swagger_client.GeocodingApi()
key = 'c9233b36-a5f6-4e05-a0e7-f1f8447f1cbb' # str | Get your key at graphhopper.com
q = 'Siward street York' # str | If you do forward geocoding, then this would be a textual description of the address you are looking for (optional)
locale = 'locale_example' # str | Display the search results for the specified locale. Currently French (fr), English (en), German (de) and Italian (it) are supported. If the locale wasn't found the default (en) is used. (optional)
limit = 56 # int | Specify the maximum number of returned results (optional)
reverse = False # bool | Set to true to do a reverse Geocoding request, see point parameter (optional)
point = 'point_example' # str | The location bias in the format 'latitude,longitude' e.g. point=45.93272,11.58803 (optional)
provider = 'provider_example' # str | Can be either, default, nominatim, opencagedata (optional)
def lookup_location(key,q,limit,locale='en'):
	try:
	    # Execute a Geocoding request
	    api_response = api_instance.geocode_get(key, q=q, locale=locale, limit=limit)
	    response = api_response.to_dict()
	    #pprint(api_response)
	    print(response['hits'][1])
	except ApiException as e:
	    print("Exception when calling GeocodingApi->geocode_get: %s\n" % e)
lookup_location(key,q,limit)
