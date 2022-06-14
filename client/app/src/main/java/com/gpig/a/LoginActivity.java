package com.gpig.a;

import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gpig.a.settings.Settings;
import com.gpig.a.utils.BiometricCallback;
import com.gpig.a.utils.BiometricUtils;
import com.gpig.a.utils.NotificationUtils;
import com.gpig.a.utils.ServerUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationUtils.isAppOpen = true;
        NotificationUtils.createNotificationChannel(this);
//        NotificationUtils.clearNotifications(this);
        if(getIntent().hasExtra("NotificationID")){
            NotificationUtils.clearNotification(this, getIntent().getIntExtra("NotificationID", 0));
        }
        setContentView(R.layout.activity_login);
        Button b = findViewById(R.id.login_button);
        b.setOnClickListener(this);
        //load settings from file
        Settings.readFromFile(this);
        ServerUtils.pollServer = new PollServer();
        ServerUtils.pollServer.setAlarm(this.getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button){
            final String email = ((EditText)findViewById(R.id.email)).getText().toString();
            if(!BiometricUtils.isSdkVersionSupported()){
                Toast.makeText(getApplicationContext(), "SDK Version not Supported", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isHardwareSupported(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "No Hardware Support", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isPermissionGranted(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "Permission is not Granted", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isFingerprintAvailable(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "No Fingerprints Registered", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isBiometricPromptEnabled()){
                Toast.makeText(getApplicationContext(), "Biometric Prompt Disabled", Toast.LENGTH_LONG).show();
            }else {
                if(!email.matches("[^@]+@[^@]+\\.[^@]+")){
                    Toast.makeText(getApplicationContext(), "Invalid email address!", Toast.LENGTH_LONG).show();
                    return;
                }
                BiometricCallback bc = new BiometricCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        myIntent.putExtra("email", email);
                        //TODO check login
//                        if(!email.equals(Settings.LastEmail)){
//                            if(StatusUtils.isNetworkAvailable(LoginActivity.this)) {
//                                //TODO new user need to user FIDO to authenticate and get a session key
//                                FIDO2Utils fu = new FIDO2Utils(LoginActivity.this);
//                                fu.sign(email);
//                                return;
//                            }else{
//                                LoginActivity.this.runOnUiThread(new Runnable() {
//                                    public void run() {
//                                        String no_network = "Network required to sync new user";//getString(R.string.login_success);
//                                        Toast.makeText(LoginActivity.this, no_network, Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                                return;
//                            }
//                        }else {
                            startActivity(myIntent);
//                        }
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String login_success = getString(R.string.login_success);
                                Toast.makeText(LoginActivity.this, login_success, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, final CharSequence helpString) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String help = getString(R.string.login_help) + helpString;
                                Toast.makeText(LoginActivity.this, help, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, final CharSequence errString) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String error = getString(R.string.login_error) + errString;
                                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String fail = getString(R.string.login_failed);
                                Toast.makeText(LoginActivity.this, fail, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationCancelled() {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String cancel = getString(R.string.login_cancelled);
                                Toast.makeText(LoginActivity.this, cancel, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                };
                BiometricUtils.displayBiometricPrompt(bc, getApplicationContext());
                return;
            }
            //TODO support other local login methods? see second half of
            // https://proandroiddev.com/5-steps-to-implement-biometric-authentication-in-android-dbeb825aeee8
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause: Login");
        NotificationUtils.isAppOpen = false;
    }

    @Override
    public void onStart(){
        Log.i(TAG, "onStart: Login");
        super.onStart();
        NotificationUtils.isAppOpen = true;
    }
}