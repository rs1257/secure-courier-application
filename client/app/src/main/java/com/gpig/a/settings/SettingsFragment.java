package com.gpig.a.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gpig.a.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener{
    private EditText serverIPText;
    private EditText serverPortText;
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_settings, container, false);
        Button b = v.findViewById(R.id.apply_settings_button);
        serverIPText = v.findViewById(R.id.server_ip);
        serverIPText.getText().clear();
        serverIPText.getText().append(Settings.ServerIP);

        serverPortText = v.findViewById(R.id.server_port);
        serverPortText.getText().clear();
        serverPortText.getText().append(String.valueOf(Settings.ServerPort));

        b.setOnClickListener(this);
        return v;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.apply_settings_button) {
            Settings.ServerIP = serverIPText.getText().toString();
            Settings.ServerPort = Integer.parseInt(serverPortText.getText().toString());
            Settings.writeToFile(getActivity());
            Toast.makeText(getContext(), "Settings Applied", Toast.LENGTH_LONG).show();
        }
    }
}
