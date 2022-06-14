from django.db import models
from controller.models import Courier as ControllerCourier

# Create your models here.


class Courier(models.Model):
    controller_model = models.OneToOneField(ControllerCourier, on_delete=models.CASCADE)
    email = models.CharField(max_length = 50)
    cred_id = models.BinaryField()
    cred_pub_key = models.BinaryField()
    registration_challenge = models.CharField(max_length = 50, default = '')
    authentication_challenge = models.CharField(max_length = 50, default = '')
    session_key = models.CharField(max_length = 70, default = '')
    one_time_key = models.CharField(max_length = 70, default = '')
    def __str__(self):
        return self.email
