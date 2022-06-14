from django.contrib import admin
from .models import Controller, Courier, Route, RouteComponent
# Register your models here.
admin.site.register(Controller)
admin.site.register(Courier)
admin.site.register(Route)
admin.site.register(RouteComponent)