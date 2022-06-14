# GPIG-Team-A
Secure Courier Application POC

## Client
The client folder contains an android studio project for the courier client side application written in Java

## Server
The Server folder contains python code to create routes
requires: https://github.com/graphhopper/directions-api-clients/tree/master/python

## mysite
The mysite folder contains the django server.

Run the http debug server with `python3 manage.py runserver 0:8000`

Run the https server with `./run.py`\
requires:
 - eventlet (GitHub version)
 - django
 - pywarp (GitHub version)
 - cbor2
 - pandas
 - numpy

Run `pip3 install -r requirements.txt` to install the required packages
