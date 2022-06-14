from django.urls import path

from . import views

urlpatterns = [
    path('<int:controller_id>/', views.controller, name='controller'),
    path('<int:controller_id>/couriers/', views.couriers, name='couriers'),
    path('route/', views.route, name='route'),
    path('routecalc/', views.routeCalc, name='routeCalc'),
    path('checkin/<int:courier_id>/', views.checkin, name='checkin'),
    path('viewroute/<int:courier_id>/', views.view_route, name = 'view_route'),
    path('update/<str:lattitude>/<str:longitude>/<int:courier_id>/', views.update, name= 'update')
]
