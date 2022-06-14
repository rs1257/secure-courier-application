from pywarp import Credential
from pywarp.backends import CredentialStorageBackend
from .models import Courier
from controller.models import Courier as ControllerCourier
from controller.models import Controller


class MyDBBackend(CredentialStorageBackend):
    def __init__(self):
        pass

    def get_credential_by_email(self, email):
        courier = Courier.objects.get(email=email)
        if len(courier.cred_pub_key) < 5:
            raise Courier.DoesNotExist()
        return Credential(credential_id=courier.cred_id,
                          credential_public_key=courier.cred_pub_key)

    def save_credential_for_user(self, email, credential):
        courier = Courier.objects.get(email=email)
        courier.cred_id = credential.id
        courier.cred_pub_key = credential.public_key.cbor_cose_key
        courier.save()

    def save_challenge_for_user(self, email, challenge, type):
        assert type in {"registration", "authentication"}
        try:
            courier = Courier.objects.get(email=email)
        except Exception as e:
            controller_model = ControllerCourier(controller=Controller.objects.get(pk=1))
            controller_model.save()
            courier = Courier(email=email, controller_model = controller_model)
        if type == "registration":
            courier.registration_challenge = challenge
        else:
            courier.authentication_challenge = challenge
        courier.save()

    def save_session_key(self, email, session_key):
        courier = Courier.objects.get(email=email)
        courier.session_key = session_key
        courier.save()

    def get_id_from_email(self, email):
        courier = Courier.objects.get(email=email)
        return courier.controller_model.pk

    def save_one_time_key(self, email, one_time_key):
        courier = Courier.objects.get(email=email)
        courier.one_time_key = one_time_key
        courier.save()

    def get_challenge_for_user(self, email, type):
        assert type in {"registration", "authentication"}
        courier = Courier.objects.get(email=email)
        if type == "registration":
            return courier.registration_challenge
        else:
            return courier.authentication_challenge
