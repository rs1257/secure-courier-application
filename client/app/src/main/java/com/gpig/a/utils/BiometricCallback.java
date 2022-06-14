package com.gpig.a.utils;

import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.P)
public abstract class BiometricCallback extends BiometricPrompt.AuthenticationCallback {


    @Override
    public abstract void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result);


    @Override
    public abstract void onAuthenticationHelp(int helpCode, CharSequence helpString) ;


    @Override
    public abstract void onAuthenticationError(int errorCode, CharSequence errString);


    @Override
    public abstract void onAuthenticationFailed();

    public abstract void onAuthenticationCancelled();
}