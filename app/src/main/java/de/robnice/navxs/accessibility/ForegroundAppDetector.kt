package de.robnice.navxs.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.provider.Settings

class ForegroundAppDetector(private val context: Context) {
    fun isAccessibilityServiceEnabled(serviceClassName: String): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledByManager = accessibilityManager
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info ->
                val serviceInfo = info.resolveInfo.serviceInfo
                serviceInfo.packageName == context.packageName && serviceInfo.name == serviceClassName
            }
        if (enabledByManager) return true

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val component = ComponentName(context.packageName, serviceClassName)
        val expectedLong = component.flattenToString()
        val expectedShort = component.flattenToShortString()
        return enabledServices.split(':').any {
            it.equals(expectedLong, ignoreCase = true) || it.equals(expectedShort, ignoreCase = true)
        }
    }
}
