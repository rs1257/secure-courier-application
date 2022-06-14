from django.shortcuts import render, HttpResponse
import json
from django.conf import settings
import os

# Create your views here.
def safety(request, locations):
    locations = locations.split(',')
    jsonfile = open(os.path.join(settings.BASE_DIR, 'safety/data/scores.json'), 'r')
    data = json.load(jsonfile)
    out = {}
    total = 0 
    for location in locations:
        if 'England' in location:
            location = 'United Kingdom (England and Wales)'
        elif 'Wales' in location:
            location = 'United Kingdom (England and Wales)'
        elif 'Scotland' in location:
            location = 'United Kingdom (Scotland)'
        elif 'Northern Ireland' in location:
            location = 'United Kingdom (Northern Ireland)'
        out[location] = data.get(location)
        total += data.get(location, 0)
    out['total'] = total
    return HttpResponse(json.dumps(out), content_type="application/json")
