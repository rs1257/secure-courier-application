from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse, HttpResponseRedirect
from .models import Controller, Route, RouteComponent, Courier
from authentication.models import Courier as Auth_Courier
from django.views.decorators.csrf import csrf_exempt
from .forms import RouteForm
import os
from django.conf import settings
import django
from pywarp.util import b64_encode, b64url_decode
import json
import time
import controller.Routetest as rt
from controller.assign_route import assign_route

def controller(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    if request.method == 'POST':
        form = RouteForm(request.POST)
        if form.is_valid():
            start_long = form.cleaned_data['start_long']
            start_lat = form.cleaned_data['start_lat']
            end_long = form.cleaned_data['end_long']
            end_lat = form.cleaned_data['end_lat']
            travel_time = int(form.cleaned_data['travel_time'])
            rt.maxtraveltime = travel_time*60*60*1000
            courier = form.cleaned_data['courier_id'].controller_model
            point1 = '%s,%s' %(start_lat,start_long)
            point2 = '%s,%s' %(end_lat,end_long)

            assign_route(courier, point1, point2)
            return HttpResponseRedirect('couriers/')
    else:
        form = RouteForm()
    context = { 'controller' : controller, 'form': form}
    return render(request, 'controller/controller.html', context)

def route(request):
    json_file = open(os.path.join(settings.BASE_DIR, 'routejson.txt'))
    data = json.load(json_file)
    return HttpResponse(json.dumps(data), content_type="application/json")

def couriers(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    context = { 'controller' : controller}
    return render(request, 'controller/couriers.html', context)

def routeCalc(request):
    lang1 = 1
    long1 = 1
    point1 = '%s,%s' %(lang1,long1)

    lang2 = 1
    long2 = 1
    point2 = '%s,%s' %(lang1,long1)
    points = [point1, point2]

    rt.makeroute(rt.point, rt.key, rt.maxtraveltime)

@csrf_exempt #TODO: This should be removed and proper CSRFs used in the android app
def checkin(request, courier_id):
    courier = get_object_or_404(Courier, pk=courier_id)

    # check authentication
    if 'one_time_key' not in request.POST:
        return HttpResponse('authentication failed')
    auth_courier = get_object_or_404(Auth_Courier,controller_model=courier)
    remote_one_time_key = request.POST.get('one_time_key').split(',')
    valid_one_time_key = auth_courier.one_time_key.split(',')
    if len(valid_one_time_key) == 0 or len(valid_one_time_key) != len(remote_one_time_key):
        return HttpResponse('authentication failed')
    if time.time() > int(remote_one_time_key[1]): # make sure key is still valid
        return HttpResponse('key no longer valid')
    if remote_one_time_key[1] != valid_one_time_key[1]: # check same key timestamp
        return HttpResponse('key doesnt match')
    if b64url_decode(remote_one_time_key[0]) != b64url_decode(valid_one_time_key[0]): # check same key
        return HttpResponse('key doesnt match')
    auth_courier.one_time_key = '' #key used so remove it
    auth_courier.save()

    route = courier.route
    if route.length == route.current:
        route.delete()
        courier.route_ready = False
        courier.save()
        return HttpResponse('no route')
    else:
        route.current += 1
        route.save()
        comp = RouteComponent.objects.filter(route=route, position=route.current)[0]
        return HttpResponse(comp.json, content_type="application/json")

@csrf_exempt #TODO: This should be removed and proper CSRFs used in the android app
def update(request, lattitude, longitude, courier_id):
    courier = get_object_or_404(Courier, pk=courier_id)

    # check authentication
    if 'session_key' not in request.POST:
        return HttpResponse('authentication failed')
    auth_courier = get_object_or_404(Auth_Courier,controller_model=courier)
    remote_session_key = request.POST.get('session_key').split(',')
    valid_session_key = auth_courier.session_key.split(',')
    if len(valid_session_key) == 0 or len(valid_session_key) != len(remote_session_key):
        return HttpResponse('authentication failed')
    if time.time() > int(remote_session_key[1]): # make sure key is still valid
        return HttpResponse('key no longer valid')
    if remote_session_key[1] != valid_session_key[1]: # check same key timestamp
        return HttpResponse('key doesnt match')
    if b64url_decode(remote_session_key[0]) != b64url_decode(valid_session_key[0]): # check same key
        return HttpResponse('key doesnt match')

    courier.lattitude = lattitude
    courier.longitude = longitude
    courier.save()
    if courier.route_ready:
        try:
            route = courier.route
        except Courier.route.RelatedObjectDoesNotExist as e:
            return HttpResponse('False')
        if route.current >0: # dont recalculate route after the courier has completed it
            return HttpResponse('False')
        old_comp = RouteComponent.objects.filter(route=route, position=0).delete()
        first_stage = RouteComponent.objects.filter(route=route, position=1)[0].json
        first_stage = json.loads(first_stage)
        first_point = first_stage['paths'][0]['points']['coordinates'][0]
        point1 = '%s,%s' %(lattitude,longitude)
        first_point_string = '%s,%s' %(first_point[1],first_point[0])
        first_comp_json = rt.makeroute([point1, first_point_string], rt.key, rt.maxtraveltime)
        comp = RouteComponent(route=route, position = 0, json=json.dumps(first_comp_json))
        comp.save()
        if route.current == 0: # only respond true if the courier hasnt checked in yet
            return HttpResponse('False')
        return HttpResponse('True')
    else:
        return HttpResponse('False')

def view_route(request, courier_id):
    courier = get_object_or_404(Courier, pk=courier_id)
    route = courier.route
    jsonroute = json.loads(RouteComponent.objects.filter(route=route, position=-1)[0].json)
    context = { 'route' :  json.dumps(jsonroute), 'courier' : courier}
    return render(request, 'controller/route.html', context)
