from django.urls import path

from . import views

urlpatterns = [
    path('getRegistrationOptions/', views.get_registration_options, name='getRegistrationOptions'),
    path('register/', views.register, name='register'),
    path('getAuthenticationOptions/', views.get_authentication_options, name='getAuthenticationOptions'),
    path('authenticate/', views.authenticate, name='authenticate'),
]
