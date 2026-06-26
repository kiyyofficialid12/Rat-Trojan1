package com.rat.premium;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextView tvDeviceId = view.findViewById(R.id.tvDeviceId);
        String id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        tvDeviceId.setText("Device ID: " + id);
        return view;
    }
}
