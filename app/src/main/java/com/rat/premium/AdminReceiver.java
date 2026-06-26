package com.rat.premium;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {
    @Override public void onEnabled(Context c, Intent i) { Toast.makeText(c, "Admin aktif", Toast.LENGTH_SHORT).show(); }
    @Override public void onDisabled(Context c, Intent i) { Toast.makeText(c, "Admin nonaktif", Toast.LENGTH_SHORT).show(); }
}
