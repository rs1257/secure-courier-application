
from pywarp.util import b64_encode, b64url_decode
from pywarp.attestation import FIDOU2FAttestationStatement
from pywarp.authenticators import AuthenticatorData
from pywarp import Credential
import json
import hashlib
import cbor2
import re


def customVerify(rp, authenticator_data, client_data_json, signature, email, user_handle='', raw_id=''):
    "Ascertain the validity of credentials supplied by the client user agent via navigator.credentials.get()"
    email = email.decode()
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        raise Exception("Invalid email address")
    client_data_hash = hashlib.sha256(client_data_json.encode('utf-8')).digest()
    client_data = json.loads(client_data_json)
    assert client_data["type"] == "webauthn.get"
    expect_challenge = rp.storage_backend.get_challenge_for_user(email=email, type="authentication")
    assert str(b64url_decode(client_data["challenge"])) == str(expect_challenge)
    # print("expect RP ID:", rp.rp_id)
    if rp.rp_id:
        assert "https://" + rp.rp_id == client_data["origin"] or 'android:apk-key-hash:' in client_data["origin"] # TODO: use full apk hash
    # Verify that the value of C.origin matches the Relying Party's origin.
    # Verify that the RP ID hash in authData is indeed the SHA-256 hash of the RP ID expected by the RP.
    authenticator_data = AuthenticatorData(authenticator_data)
    assert authenticator_data.user_present
    credential = rp.storage_backend.get_credential_by_email(email)
    credential.verify(signature, authenticator_data.raw_auth_data + client_data_hash)
    # signature counter check
    return {"verified": True}

def customRegister(rp, client_data_json, attestation_object, email):
    "Store the credential public key and related metadata on the server using the associated storage backend"
    authenticator_attestation_response = cbor2.loads(attestation_object)
    email = email.decode()
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        raise Exception("Invalid email address")
    client_data_hash = hashlib.sha256(client_data_json.encode('utf-8')).digest()
    client_data = json.loads(client_data_json)
    assert client_data["type"] == "webauthn.create"
    # print("client data", client_data)
    expect_challenge = rp.storage_backend.get_challenge_for_user(email=email, type="registration")
    assert str(b64url_decode(client_data["challenge"])) == str(expect_challenge)
    # print("expect RP ID:", rp.rp_id)
    if rp.rp_id:
        assert "https://" + rp.rp_id == client_data["origin"] or 'android:apk-key-hash:' in client_data["origin"] # TODO: use full apk hash
    # Verify that the value of C.origin matches the Relying Party's origin.
    # Verify that the RP ID hash in authData is indeed the SHA-256 hash of the RP ID expected by the RP.
    authenticator_data = AuthenticatorData(authenticator_attestation_response["authData"])
    assert authenticator_data.user_present
    # If user verification is required for this registration,
    # verify that the User Verified bit of the flags in authData is set.
    if authenticator_attestation_response["fmt"] == "fido-u2f":
        att_stmt = FIDOU2FAttestationStatement(authenticator_attestation_response['attStmt'])
        attestation = att_stmt.validate(authenticator_data,
                                        rp_id_hash=authenticator_data.rp_id_hash,
                                        client_data_hash=client_data_hash)
        credential = attestation.credential
    elif authenticator_attestation_response["fmt"] == "android-safetynet":
        header, payload, signature = authenticator_attestation_response['attStmt']['response'].decode().split('.')
        #INFO at https://medium.com/@herrjemand/verifying-fido2-safetynet-attestation-bd261ce1978d
        #verify payload
        payload_json = json.loads(b64url_decode(payload).decode())
        nonce_base = authenticator_attestation_response["authData"] + client_data_hash
        nonce_buffer = hashlib.sha256(nonce_base).digest()
        expected_nonce = b64_encode(nonce_buffer)
        assert payload_json["nonce"] == expected_nonce
        assert payload_json["ctsProfileMatch"]

        #TODO: verify header
        header_json = json.loads(b64url_decode(header).decode())

        #TODO: verify JWT
        #TODO: verify cert chain
        # att_stmt = FIDOU2FAttestationStatement(header_json)
        # attestation = att_stmt.validate(authenticator_data,
        #                                 rp_id_hash=authenticator_data.rp_id_hash,
        #                                 client_data_hash=client_data_hash)
        # credential = attestation.credential
        credential = authenticator_data.credential

    # TODO: ascertain user identity here
    rp.storage_backend.save_credential_for_user(email=email, credential=credential)
    return {"registered": True}
