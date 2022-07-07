package com.jgl.ratptoilets.ui.theme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


    fun onRequestPermission(context: Context, onRefused: (() -> Unit)?, onGranted: () -> Unit){
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                onGranted()
            }
            else -> {
                onRefused?.invoke()
            }
        }
    }
