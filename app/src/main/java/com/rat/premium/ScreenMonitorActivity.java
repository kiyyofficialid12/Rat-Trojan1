package com.rat.premium;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;

public class ScreenMonitorActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1001;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler = new Handler();
    private boolean isMonitoring = false;
    private ImageView preview;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_monitor);

        preview = findViewById(R.id.screenPreview);
        Button btnStart = findViewById(R.id.btnStartMonitor);
        Button btnStop = findViewById(R.id.btnStopMonitor);
        Button btnCapture = findViewById(R.id.btnCaptureScreen);
        Button btnClose = findViewById(R.id.btnClose);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        btnStart.setOnClickListener(v -> {
            if (!isMonitoring) {
                startScreenCapture();
            }
        });

        btnStop.setOnClickListener(v -> {
            if (isMonitoring) {
                stopScreenCapture();
            }
        });

        btnCapture.setOnClickListener(v -> captureScreen());
        btnClose.setOnClickListener(v -> finish());
    }

    private void startScreenCapture() {
        Intent intent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int density = metrics.densityDpi;

            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    width, height, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null, null
            );

            isMonitoring = true;
            Toast.makeText(this, "Screen monitoring started", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (isMonitoring) {
                        captureScreen();
                        handler.postDelayed(this, 3000);
                    }
                }
            }, 2000);
        } else {
            Toast.makeText(this, "Izin screen capture ditolak", Toast.LENGTH_SHORT).show();
        }
    }

    private void captureScreen() {
        if (imageReader == null) return;
        Image image = imageReader.acquireLatestImage();
        if (image != null) {
            Image.Plane plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            int width = image.getWidth();
            int height = image.getHeight();
            int pixelStride = plane.getPixelStride();
            int rowStride = plane.getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            preview.setImageBitmap(cropped);
            image.close();
        }
    }

    private void stopScreenCapture() {
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        Toast.makeText(this, "Screen monitoring stopped", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopScreenCapture();
    }
}
