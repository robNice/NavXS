package de.robnice.navxs.overlay

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
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

    fun show(settings: OverlaySettings) {
        if (showing && lastRenderedSettings == settings) {
            Log.d(TAG, "showSkipped unchangedSettings active=${settings.buttons.values.count { it.active }}")
            return
        }
        Log.d(TAG, "show active=${settings.buttons.values.count { it.active }} showing=$showing")
        render(settings)
        lastRenderedSettings = settings
        showing = true
    }

    fun hide() {
        if (!showing && buttonViews.isEmpty()) return
        Log.d(TAG, "hide attached=${buttonViews.values.count { it.isAttachedToWindow }} total=${buttonViews.size}")
        buttonViews.values.toList().forEach { view ->
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
        }
        buttonViews.clear()
        lastRenderedSettings = null
        showing = false
    }

    private fun render(settings: OverlaySettings) {
        val activeButtons = settings.buttons.values.filter { it.active }
        val activeTypes = activeButtons.map { it.type }.toSet()

        buttonViews.entries.toList().forEach { (type, view) ->
            if (type !in activeTypes) {
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
                buttonViews.remove(type)
            }
        }

        activeButtons.forEach { button ->
            val existingView = buttonViews[button.type]
            if (existingView != null && existingView.isAttachedToWindow) {
                bindButtonView(existingView, button)
                Log.d(TAG, "render update type=${button.type} x=${button.positionXPx} y=${button.positionYPx}")
                windowManager.updateViewLayout(existingView, layoutParams(button))
            } else {
                createButtonView(button).also { view ->
                    buttonViews[button.type] = view
                    Log.d(TAG, "render add type=${button.type} x=${button.positionXPx} y=${button.positionYPx}")
                    windowManager.addView(view, layoutParams(button))
                }
            }
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
        var activePressId = 0L
        var downUptimeMs = 0L
        view.setOnTouchListener { touchedView, event ->
            val isWithinBounds = event.x >= 0f &&
                event.y >= 0f &&
                event.x <= touchedView.width &&
                event.y <= touchedView.height
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
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
        val density = context.resources.displayMetrics.density
        val touchTargetPx = touchTargetPx(button.sizePercent, density)
        return WindowManager.LayoutParams(
            touchTargetPx,
            touchTargetPx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = button.positionXPx
            y = button.positionYPx
            layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private companion object {
        const val TAG = "OverlayController"
        const val VIEW_ID_BACKGROUND = 1000
        const val VIEW_ID_ICON = 1001
        const val VIEW_ID_TEXT = 1002
    }
}

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
