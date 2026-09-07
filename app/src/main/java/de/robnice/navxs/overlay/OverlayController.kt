package de.robnice.navxs.overlay

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import de.robnice.navxs.R
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.domain.ThemeRegistry
import de.robnice.navxs.ui.settings.backgroundSizeDp
import de.robnice.navxs.ui.theme.themeDrawableRes
import kotlin.math.max

class OverlayController(
    private val context: Context,
    private val onButtonPress: (NavButtonType, Long) -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val buttonViews = mutableMapOf<NavButtonType, FrameLayout>()
    private var lastRenderedSettings: OverlaySettings? = null
    private var showing = false
    private var nextPressId = 1L
    private val idleHandler = Handler(Looper.getMainLooper())
    private val idleRunnable = Runnable { enterIdleMode() }
    private var burnInProtectionEnabled = false
    private var screenInteractive = true
    private var idle = false

    /** Enables or disables the global burn-in protection at runtime. */
    fun setBurnInProtectionEnabled(enabled: Boolean) {
        if (burnInProtectionEnabled == enabled) return
        burnInProtectionEnabled = enabled
        if (enabled) {
            restartIdleTimer()
        } else {
            cancelIdleTimer()
            leaveIdleMode()
        }
    }

    /** Keeps the protection timer from running while the screen is off. */
    fun setScreenInteractive(interactive: Boolean) {
        if (screenInteractive == interactive) return
        screenInteractive = interactive
        if (interactive) restartIdleTimer() else cancelIdleTimer()
    }

    fun show(settings: OverlaySettings) {
        Log.d(TAG, "show active=${settings.buttons.values.count { it.active }} showing=$showing")
        if (render(settings)) {
            lastRenderedSettings = settings
            showing = true
            if (idle) applyIdleAppearance() else restartIdleTimer()
        } else {
            hide()
        }
    }

    fun hide() {
        if (!showing && buttonViews.isEmpty()) return
        Log.d(TAG, "hide attached=${buttonViews.values.count { it.isAttachedToWindow }} total=${buttonViews.size}")
        buttonViews.values.toList().forEach { view ->
            safelyRemoveView(view)
        }
        buttonViews.clear()
        lastRenderedSettings = null
        showing = false
        cancelIdleTimer()
        idle = false
    }

    private fun restartIdleTimer() {
        cancelIdleTimer()
        if (!burnInProtectionEnabled || !screenInteractive || !showing) return
        idleHandler.postDelayed(idleRunnable, IDLE_DELAY_MS)
    }

    private fun cancelIdleTimer() {
        idleHandler.removeCallbacks(idleRunnable)
    }

    private fun enterIdleMode() {
        if (!burnInProtectionEnabled || !showing) return
        idle = true
        applyIdleAppearance()
    }

    private fun leaveIdleMode() {
        if (!idle) return
        idle = false
        lastRenderedSettings?.let { settings ->
            settings.buttons.values.filter { it.active }.forEach { button ->
                buttonViews[button.type]?.let { view -> applyAppearance(view, button, idle = false) }
            }
        }
    }

    private fun applyIdleAppearance() {
        val settings = lastRenderedSettings ?: return
        settings.buttons.values.filter { it.active }.forEach { button ->
            buttonViews[button.type]?.let { view -> applyAppearance(view, button, idle = true) }
        }
    }

    /**
     * Applies the runtime-only appearance. In idle mode the button background is hidden completely
     * and the icon keeps at most the configured opacity, reduced by [IDLE_OPACITY_FACTOR].
     */
    private fun applyAppearance(view: FrameLayout, button: OverlayButtonConfig, idle: Boolean) {
        val backgroundView = view.findViewById<FrameLayout>(VIEW_ID_BACKGROUND)
        val imageView = view.findViewById<ImageView>(VIEW_ID_ICON)
        val textView = view.findViewById<TextView>(VIEW_ID_TEXT)
        val effectiveOpacity = if (idle) idleOpacity(button.opacity) else button.opacity
        backgroundView.visibility = when {
            idle -> FrameLayout.GONE
            button.backgroundOpacity > 0f -> FrameLayout.VISIBLE
            else -> FrameLayout.GONE
        }
        if (imageView.visibility == ImageView.VISIBLE) {
            imageView.alpha = effectiveOpacity
        }
        if (textView.visibility == TextView.VISIBLE) {
            textView.setTextColor(applyAlpha(button.colorArgb.toInt(), effectiveOpacity))
        }
    }

    private fun onUserInteraction() {
        if (idle) leaveIdleMode()
        restartIdleTimer()
    }

    private fun render(settings: OverlaySettings): Boolean {
        val activeButtons = settings.buttons.values
            .filter { it.active }
        val activeTypes = activeButtons.map { it.type }.toSet()

        buttonViews.entries.toList().forEach { (type, view) ->
            if (type !in activeTypes) {
                safelyRemoveView(view)
                buttonViews.remove(type)
            }
        }

        activeButtons.forEach { button ->
            val existingView = buttonViews[button.type]
            if (existingView != null && existingView.windowToken != null) {
                bindButtonView(existingView, button)
                Log.d(TAG, "render update type=${button.type} x=${button.positionXPx} y=${button.positionYPx}")
                if (!safelyUpdateView(existingView, button)) return false
            } else {
                if (existingView != null) {
                    safelyRemoveView(existingView)
                }
                createButtonView(button).also { view ->
                    Log.d(TAG, "render add type=${button.type} x=${button.positionXPx} y=${button.positionYPx}")
                    if (!safelyAddView(view, button)) return false
                    buttonViews[button.type] = view
                }
            }
        }
        return true
    }

    private fun safelyAddView(view: FrameLayout, button: OverlayButtonConfig): Boolean = try {
        windowManager.addView(view, layoutParams(button))
        true
    } catch (error: BadTokenException) {
        Log.w(TAG, "add skipped: accessibility window token is no longer valid", error)
        false
    } catch (error: SecurityException) {
        Log.w(TAG, "add skipped: accessibility overlay is no longer permitted", error)
        false
    }

    private fun safelyUpdateView(view: FrameLayout, button: OverlayButtonConfig): Boolean = try {
        windowManager.updateViewLayout(view, layoutParams(button))
        true
    } catch (error: IllegalArgumentException) {
        Log.w(TAG, "update skipped: overlay view is no longer attached", error)
        false
    } catch (error: BadTokenException) {
        Log.w(TAG, "update skipped: accessibility window token is no longer valid", error)
        false
    }

    private fun safelyRemoveView(view: FrameLayout) {
        view.visibility = FrameLayout.GONE
        runCatching {
            windowManager.removeViewImmediate(view)
        }.onFailure { error ->
            Log.d(TAG, "removeSkipped attached=${view.isAttachedToWindow} token=${view.windowToken != null} error=${error::class.java.simpleName}")
        }
    }

    private fun createButtonView(button: OverlayButtonConfig): FrameLayout {
        val backgroundView = FrameLayout(context).apply {
            id = VIEW_ID_BACKGROUND
        }
        val imageView = ImageView(context).apply {
            id = VIEW_ID_ICON
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val textView = TextView(context).apply {
            id = VIEW_ID_TEXT
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        return FrameLayout(context).apply {
            addView(
                backgroundView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
            addView(
                imageView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
            addView(
                textView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
        }.also { bindButtonView(it, button) }
    }

    private fun bindButtonView(
        view: FrameLayout,
        button: OverlayButtonConfig
    ) {
        val density = context.resources.displayMetrics.density
        val theme = ThemeRegistry().resolve(button.type, button.themeId)
        val iconSizeDp = overlayIconSizeDp(button.sizePercent)
        val iconSizePx = iconSizePx(button.sizePercent, density)
        val backgroundSizePx = withBackgroundSizePx(iconSizeDp, button.backgroundSizePercent, density)
        val backgroundView = view.findViewById<FrameLayout>(VIEW_ID_BACKGROUND)
        val imageView = view.findViewById<ImageView>(VIEW_ID_ICON)
        val textView = view.findViewById<TextView>(VIEW_ID_TEXT)
        view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        view.isClickable = true
        view.isFocusable = false
        view.minimumWidth = iconSizePx
        view.minimumHeight = iconSizePx
        backgroundView.visibility = if (button.backgroundOpacity > 0f) FrameLayout.VISIBLE else FrameLayout.GONE
        backgroundView.layoutParams = (backgroundView.layoutParams as FrameLayout.LayoutParams).apply {
            width = backgroundSizePx
            height = backgroundSizePx
            gravity = Gravity.CENTER
        }
        backgroundView.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.TRANSPARENT)
        }
        backgroundView.background = createBackgroundDrawable(
            sizePx = backgroundSizePx,
            color = button.backgroundColorArgb.toInt(),
            opacity = button.backgroundOpacity,
            softnessPercent = button.backgroundSoftnessPercent
        )
        val drawableRes = themeDrawableRes(theme.vectorAssetName)
        if (drawableRes != null) {
            imageView.setImageResource(drawableRes)
            imageView.imageTintList = ColorStateList.valueOf(button.colorArgb.toInt())
            imageView.alpha = button.opacity
            imageView.visibility = ImageView.VISIBLE
            imageView.layoutParams = (imageView.layoutParams as FrameLayout.LayoutParams).apply {
                width = iconSizePx
                height = iconSizePx
                gravity = Gravity.CENTER
            }
            textView.visibility = TextView.GONE
            textView.text = ""
        } else {
            imageView.setImageDrawable(null)
            imageView.visibility = ImageView.GONE
            textView.visibility = TextView.VISIBLE
            textView.text = theme.fallbackText
            textView.setTextColor(applyAlpha(button.colorArgb.toInt(), button.opacity))
            textView.textSize = fallbackFontSizeSp(iconSizeDp)
        }
        imageView.invalidate()
        textView.invalidate()
        backgroundView.invalidate()
        view.requestLayout()
        view.invalidate()
        var activePressId = 0L
        var downUptimeMs = 0L
        view.setOnTouchListener { touchedView, event ->
            val isWithinBounds = event.x >= 0f &&
                event.y >= 0f &&
                event.x <= touchedView.width &&
                event.y <= touchedView.height
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onUserInteraction()
                    activePressId = nextPressId++
                    downUptimeMs = event.eventTime
                    Log.d(
                        TAG,
                        "touchDown pressId=$activePressId type=${button.type} rawX=${event.rawX} rawY=${event.rawY} width=${touchedView.width} height=${touchedView.height}"
                    )
                    touchedView.isPressed = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.d(
                        TAG,
                        "touchMove pressId=$activePressId type=${button.type} within=$isWithinBounds x=${event.x} y=${event.y}"
                    )
                    touchedView.isPressed = isWithinBounds
                    true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    val shouldTrigger = event.actionMasked == MotionEvent.ACTION_UP &&
                        touchedView.isPressed &&
                        isWithinBounds
                    Log.d(
                        TAG,
                        "touchEnd pressId=$activePressId type=${button.type} action=${event.actionMasked} shouldTrigger=$shouldTrigger heldMs=${event.eventTime - downUptimeMs}"
                    )
                    touchedView.isPressed = false
                    if (shouldTrigger) {
                        onButtonPress(button.type, activePressId)
                    }
                    activePressId = 0L
                    downUptimeMs = 0L
                    true
                }
                else -> true
            }
        }
    }

    private fun layoutParams(button: OverlayButtonConfig): WindowManager.LayoutParams {
        val density = OverlayViewport.metrics(context).density
        val touchTargetPx = touchTargetPx(button.sizePercent, density)
        return WindowManager.LayoutParams(
            touchTargetPx,
            touchTargetPx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = button.positionXPx
            y = button.positionYPx
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
    }

    private companion object {
        const val TAG = "OverlayController"
        const val VIEW_ID_BACKGROUND = 1000
        const val VIEW_ID_ICON = 1001
        const val VIEW_ID_TEXT = 1002
    }
}

/**
 * Effective icon opacity while the burn-in protection rests. Never higher than the configured value.
 */
internal fun idleOpacity(opacity: Float): Float =
    (opacity.coerceIn(0f, 1f) * IDLE_OPACITY_FACTOR).coerceAtMost(opacity.coerceIn(0f, 1f))

internal const val IDLE_OPACITY_FACTOR = 0.35f
internal const val IDLE_DELAY_MS = 45_000L

internal fun iconSizePx(sizePercent: Int, density: Float): Int =
    max(((32 * sizePercent) / 100f * density).toInt(), (16 * density).toInt())

internal fun touchTargetPx(sizePercent: Int, density: Float): Int =
    max(iconSizePx(sizePercent, density), (56 * density).toInt())

private fun overlayIconSizeDp(sizePercent: Int): Int = max((32 * sizePercent) / 100, 16)

private fun withBackgroundSizePx(iconSizeDp: Int, sizePercent: Int, density: Float): Int =
    (backgroundSizeDp(iconSizeDp, sizePercent) * density).toInt()

private fun fallbackFontSizeSp(iconSizeDp: Int): Float = iconSizeDp * 0.7f

private fun createBackgroundDrawable(
    sizePx: Int,
    color: Int,
    opacity: Float,
    softnessPercent: Int
): android.graphics.drawable.Drawable {
    val appliedColor = applyAlpha(color, opacity)
    val softness = (softnessPercent.coerceIn(0, 100) / 100f)
    if (softness <= 0f) {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(appliedColor)
        }
    }
    val innerStop = (1f - softness).coerceIn(0f, 1f)
    return ShapeDrawable(OvalShape()).apply {
        shaderFactory = object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                val radius = minOf(width, height) / 2f
                return RadialGradient(
                    width / 2f,
                    height / 2f,
                    radius,
                    intArrayOf(appliedColor, appliedColor, applyAlpha(color, 0f)),
                    floatArrayOf(0f, innerStop, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }
        intrinsicWidth = sizePx
        intrinsicHeight = sizePx
    }
}

private fun applyAlpha(color: Int, opacity: Float): Int {
    val alpha = (opacity.coerceIn(0f, 1f) * 255).toInt()
    return (color and 0x00FFFFFF) or (alpha shl 24)
}
