package de.robnice.navxs.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.util.Log
import de.robnice.navxs.data.models.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {
    suspend fun loadApps(showSystemApps: Boolean, enabledPackages: Set<String>): List<InstalledAppInfo> {
        return withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            packageManager.getInstalledApplications(0)
                .map { appInfo ->
                    val systemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val safeLabel = runCatching {
                        packageManager.getApplicationLabel(appInfo).toString()
                    }.getOrElse {
                        Log.w(TAG, "Failed to load label for ${appInfo.packageName}", it)
                        appInfo.packageName
                    }
                    InstalledAppInfo(
                        appName = safeLabel,
                        packageName = appInfo.packageName,
                        icon = loadSafeIcon(appInfo),
                        systemApp = systemApp,
                        enabled = enabledPackages.contains(appInfo.packageName)
                    )
                }
                .filterNot { it.packageName == context.packageName }
                .filter { showSystemApps || !it.systemApp }
                .sortedWith(compareByDescending<InstalledAppInfo> { it.enabled }.thenBy { it.appName.lowercase() })
        }
    }

    private fun loadSafeIcon(appInfo: ApplicationInfo): Drawable? {
        if (appInfo.packageName == "com.android.systemui") {
            Log.w(TAG, "Skipping icon load for ${appInfo.packageName} due to resource failures")
            return null
        }
        return runCatching {
            context.packageManager.getApplicationIcon(appInfo)
        }.getOrElse {
            Log.w(TAG, "Failed to load icon for ${appInfo.packageName}", it)
            null
        }
    }

    private companion object {
        const val TAG = "InstalledAppsRepo"
    }
}
