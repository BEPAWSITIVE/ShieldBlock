package com.shieldblock.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ShieldDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "ShieldBlock Device Protection Activated", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "WARNING: Disabling Device Administration will turn off anti-uninstall and bypass protection for ShieldBlock."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "ShieldBlock Device Protection Deactivated", Toast.LENGTH_SHORT).show()
    }
}
