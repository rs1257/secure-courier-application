package com.gpig.a;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gpig.a.utils.FIDO2Utils;
import com.gpig.a.utils.FileUtils;
import com.gpig.a.utils.RouteUtils;
import com.gpig.a.utils.StatusUtils;

//TODO: check conditions and update check in display
public class CheckInFragment extends Fragment implements View.OnClickListener {

    private final String TAG = "TicketFragment";

    public static CheckInFragment newInstance() {
        return new CheckInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.check_in_fragment, container, false);
        Button checkInButton = v.findViewById(R.id.check_in_button);
        checkInButton.setOnClickListener(this);
        Button b = v.findViewById(R.id.register_button);
        b.setOnClickListener(this);
        b = v.findViewById(R.id.verify);
        b.setOnClickListener(this);

        ImageView networkIcon = v.findViewById(R.id.network_icon);
        TextView networkText = v.findViewById(R.id.network_text);
        ImageView locaationIcon = v.findViewById(R.id.location_icon);
        TextView locationText = v.findViewById(R.id.location_text);
        ImageView locationCorrectIcon = v.findViewById(R.id.location_correct_icon);
        TextView locationCorrectText = v.findViewById(R.id.location_correct_text);

        if(StatusUtils.isNetworkAvailable(getActivity())){
            networkIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_wifi));
            networkText.setText(R.string.check_in_network_connected);
        }else{
            networkIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_wifi_off));
            networkText.setText(R.string.check_in_network_not_connected);
        }
        switch (StatusUtils.isLocationAvailable(getActivity())){
            case FOUND:
                locaationIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_location_found));
                locationText.setText(R.string.check_in_location_found);
                break;
            case NO_PERMISSION:
                locaationIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_location_disabled));
                locationText.setText(R.string.check_in_location_no_permission);
                break;
            case DISABLED:
                locaationIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_location_disabled));
                locationText.setText(R.string.check_in_location_disabled);
                break;
            case SEARCHING:
                locaationIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_location_searching));
                locationText.setText(R.string.check_in_location_searching);
                break;
        }
        if(StatusUtils.isLocationCorrect(getActivity())){
            locationCorrectIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_check_circle_outline));
            locationCorrectText.setText(R.string.check_in_location_correct);
        }else{
            locationCorrectIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_error_outline));
            locationCorrectText.setText(R.string.check_in_location_incorrect);
        }

        if(StatusUtils.hasNewRoute(getActivity())){
            checkInButton.setEnabled(true);
            return v;
        }

        if(StatusUtils.canCheckIn(getActivity())){
            checkInButton.setEnabled(true);
        }else {
            checkInButton.setEnabled(false);
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.check_in_button){
            FIDO2Utils fu = new FIDO2Utils(getActivity());
            fu.sign(MainActivity.email);
        }else if(v.getId() == R.id.register_button){
            FIDO2Utils fu = new FIDO2Utils(getActivity());
            fu.register(MainActivity.email);
        }else if(v.getId() == R.id.verify){
            FIDO2Utils fu = new FIDO2Utils(getActivity());
            fu.sign(MainActivity.email);
        }
    }
}
