package com.rat.premium;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class LiveCameraActivity extends AppCompatActivity {
    private TextureView textureView;
    private CameraManager cameraManager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private boolean isFront = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera);

        textureView = findViewById(R.id.textureView);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        Button btnSwitch = findViewById(R.id.btnSwitchCam);
        Button btnCapture = findViewById(R.id.btnCapture);
        Button btnClose = findViewById(R.id.btnClose);

        btnSwitch.setOnClickListener(v -> {
            isFront = !isFront;
            closeCamera();
            openCamera();
        });

        btnCapture.setOnClickListener(v -> {
            Toast.makeText(this, "📸 Captured (simulasi)", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            cameraId = isFront ? getFrontCameraId() : getBackCameraId();
            if (cameraId == null) {
                Toast.makeText(this, "Kamera tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }
                @Override public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); }
                @Override public void onError(@NonNull CameraDevice camera, int error) { camera.close(); }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            Surface surface = new Surface(textureView.getSurfaceTexture());
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) { e.printStackTrace(); }
                }
                @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(LiveCameraActivity.this, "Gagal konfigurasi", Toast.LENGTH_SHORT).show();
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) { e.printStackTrace(); }
    }

    private String getBackCameraId() throws CameraAccessException {
        for (String id : cameraManager.getCameraIdList()) {
            if (cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                    == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    private String getFrontCameraId() throws CameraAccessException {
        for (String id : cameraManager.getCameraIdList()) {
            if (cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                    == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }

    private void closeCamera() {
        if (captureSession != null) { captureSession.close(); captureSession = null; }
        if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
    }

    @Override protected void onPause() { super.onPause(); closeCamera(); }
    @Override protected void onResume() { super.onResume(); if (textureView.isAvailable()) openCamera(); }
    @Override protected void onDestroy() { super.onDestroy(); closeCamera(); }
}
