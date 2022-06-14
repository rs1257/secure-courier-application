from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Courier
from pywarp.util import b64_encode, b64url_decode
from pywarp.util.compat import token_bytes
from pywarp import RelyingPartyManager, Credential
from .FIDO2_utils import customVerify, customRegister
from .backend import MyDBBackend
import json
import time


#TODO Move from session_keys to JSON Web Tokens(JWT)

def generate_key(seconds_valid):
    return b64_encode(token_bytes(32)) + "," + str(time.time() + seconds_valid).split(".")[0]

rp_id = "tg0.uk:49300"  # This must match the origin domain of your app, as seen by the browser.
rp = RelyingPartyManager("GPIG A", rp_id=rp_id, credential_storage_backend=MyDBBackend())

def get_registration_options(request):
    if "display_name" in request.GET:
        opts = rp.get_registration_options(email=request.GET.get('courier_email'), display_name=request.GET.get('display_name'))
    else:
        opts = rp.get_registration_options(email=request.GET.get('courier_email'))
    return HttpResponse(json.dumps(opts), content_type="application/json")

@csrf_exempt #TODO: This should be removed and proper CSRFs used
def register(request):
    attestation_object = b64url_decode(request.POST.get('attestation_object'))
    client_data_json = request.POST.get('client_data_json')
    email = request.POST.get('courier_email').encode('utf-8')
    result = customRegister(rp, attestation_object=attestation_object, client_data_json=client_data_json, email=email)
    return HttpResponse(json.dumps(result), content_type="application/json")

def get_authentication_options(request):
    try:
        opts = rp.get_authentication_options(email=request.GET.get('courier_email'))
        opts['challenge'] = b64_encode(opts['challenge'])
        opts['rpId'] = rp_id
        print(opts)
        return HttpResponse(json.dumps(opts), content_type="application/json")
    except Courier.DoesNotExist as e:
        return HttpResponse(json.dumps({'errorID':1,'reason':'Courier not found'}), content_type="application/json")


@csrf_exempt #TODO: This should be removed and proper CSRFs used
def authenticate(request):
    authenticator_data = b64url_decode(request.POST.get('authenticator_data'))
    client_data_json = request.POST.get('client_data_json')
    signature = b64url_decode(request.POST.get('signature'))
    email = request.POST.get('courier_email').encode('utf-8')
    result = customVerify(rp, authenticator_data=authenticator_data, client_data_json=client_data_json, signature=signature, email=email)
    if 'check_in' in request.POST:
        print(request.POST.get('check_in'))
    if result["verified"]:
        session_key = generate_key(60*60*24) # valid for 24H
        result['session_key'] = session_key
        rp.storage_backend.save_session_key(email.decode(), session_key)
        one_time_key = generate_key(60) # valid for 1 minute
        result['one_time_key'] = one_time_key
        rp.storage_backend.save_one_time_key(email.decode(), one_time_key)
        result['user_id'] = rp.storage_backend.get_id_from_email(email.decode())
    return HttpResponse(json.dumps(result), content_type="application/json")

def assetlinks(request):
    data = [{
          "relation": ["delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"],
          "target": {
            "namespace": "web",
            "site": "https://tg0.uk:49300"
          }
        }, {
          "relation": ["delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"],
          "target": {
            "namespace": "android_app",
            "package_name": "com.gpig.a",
            "sha256_cert_fingerprints": ["5E:D1:5C:16:FA:28:8B:9C:BF:19:C5:AF:32:D4:51:EF:40:BA:05:B7:40:97:D9:5B:1C:60:9F:63:A4:E5:C4:D6","2E:22:70:2C:26:9E:87:5D:E4:60:5E:A5:FC:85:90:CC:AC:C3:8A:85:E4:9D:67:96:2A:BB:CC:0D:EC:F1:4B:E8"]
          }
        }]
    return HttpResponse(json.dumps(data), content_type="application/json")
