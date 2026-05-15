package de.robnice.navxs.data.models

import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val systemApp: Boolean,
    val enabled: Boolean
)
