from django.urls import path

from . import views

urlpatterns = [
    path('assetlinks.json', views.assetlinks, name='assetlinks'),
]
