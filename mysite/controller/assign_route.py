from controller.models import Route, RouteComponent, Courier
import controller.Routetest as rt
from controller.breakuproute import break_up_route
import json

def assign_route(courier, start_point, end_point):
    #courier = Courier.objects.filter(id=courier_id)[0]
    old_route = Route.objects.filter(courier=courier).delete()
    courier.route_ready = True
    courier.save()
    route = rt.makeroute([start_point, end_point], rt.key, rt.maxtraveltime)
    sections = [start_point]
    for section in break_up_route(route, rt.maxtraveltime):
        sections.append(section)
    sections.append(end_point)
    routeDB = Route(courier=courier, length= len(sections)-1, current=-1)
    routeDB.save()
    full = RouteComponent(route=routeDB, json=json.dumps(route), position=-1)
    full.save()
    for i in range(len(sections)-1):
        compjson = rt.makeroute([sections[i], sections[i+1]],  rt.key, rt.maxtraveltime)
        print(compjson)
        component = RouteComponent(route=routeDB, json=json.dumps(compjson), position=i+1)
        component.save()
