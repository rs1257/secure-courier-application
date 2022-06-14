#!/usr/bin/env python3
from eventlet import wsgi
import eventlet
from mysite.wsgi import application as app
from django.conf import settings

#enable https
settings.SECURE_SSL_REDIRECT = True
settings.SESSION_COOKIE_SECURE = True
settings.CSRF_COOKIE_SECURE = True
settings.SECURE_HSTS_SECONDS = 3600

wsgi.server(
    eventlet.wrap_ssl(eventlet.listen(('', 8000)), certfile='cert.pem', keyfile='key.pem', server_side=True),
    app)
