from django.urls import path

from . import views

urlpatterns = [
    path('<str:locations>/', views.safety, name='safety'),
]