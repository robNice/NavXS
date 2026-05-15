package de.robnice.navxs.overlay

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import de.robnice.navxs.R
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.domain.ThemeRegistry
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
        val imageView = view.findViewById<ImageView>(VIEW_ID_ICON)
        val textView = view.findViewById<TextView>(VIEW_ID_TEXT)
        view.alpha = button.opacity
        view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        view.isClickable = true
        view.isFocusable = false
        view.minimumWidth = iconSizePx
        view.minimumHeight = iconSizePx
        val drawableRes = overlayThemeDrawableRes(theme.vectorAssetName)
        if (drawableRes != null) {
            imageView.setImageResource(drawableRes)
            imageView.imageTintList = ColorStateList.valueOf(button.colorArgb.toInt())
            imageView.visibility = ImageView.VISIBLE
            imageView.layoutParams = (imageView.layoutParams as FrameLayout.LayoutParams).apply {
                width = iconSizePx
                height = iconSizePx
                gravity = Gravity.CENTER
            }
            imageView.setPadding((iconSizePx * 0.08f).toInt())
            textView.visibility = TextView.GONE
            textView.text = ""
        } else {
            imageView.setImageDrawable(null)
            imageView.visibility = ImageView.GONE
            textView.visibility = TextView.VISIBLE
            textView.text = theme.fallbackText
            textView.setTextColor(button.colorArgb.toInt())
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
        val iconSizePx = iconSizePx(button.sizePercent, density)
        val touchTargetPx = touchTargetPx(button.sizePercent, density)
        val insetPx = ((touchTargetPx - iconSizePx) / 2f).toInt()
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
            x = button.positionXPx - insetPx
            y = button.positionYPx - insetPx
        }
    }

    private companion object {
        const val TAG = "OverlayController"
        const val VIEW_ID_ICON = 1001
        const val VIEW_ID_TEXT = 1002
    }
}

internal fun iconSizePx(sizePercent: Int, density: Float): Int =
    max(((32 * sizePercent) / 100f * density).toInt(), (16 * density).toInt())

internal fun touchTargetPx(sizePercent: Int, density: Float): Int =
    max(
        ((if (sizePercent > 100) 32 * sizePercent / 100 else 56) * density).toInt(),
        88
    )

private fun overlayIconSizeDp(sizePercent: Int): Int = max((32 * sizePercent) / 100, 16)

private fun fallbackFontSizeSp(iconSizeDp: Int): Float = iconSizeDp * 0.7f

private fun overlayThemeDrawableRes(vectorAssetName: String): Int? = when (vectorAssetName) {
    "ArrowBackNew" -> R.drawable.overlay_arrow_back_new
    "ArrowBackNewFilled" -> R.drawable.overlay_arrow_back_new_filled
    "ArrowLeft" -> R.drawable.overlay_arrow_left
    "ArrowBackIosNew" -> R.drawable.overlay_arrow_back_ios_new
    "ArrowBack" -> R.drawable.overlay_arrow_back_classic
    "RadioButtonUnchecked" -> R.drawable.overlay_home_circle
    "Circle" -> R.drawable.overlay_home_circle_filled
    "HomeSquircleOutline" -> R.drawable.overlay_home_squircle_outline
    "HomeSquircleFilled" -> R.drawable.overlay_home_squircle_filled
    "HomeOutlined" -> R.drawable.overlay_home_house
    "Home" -> R.drawable.overlay_home_house_filled
    "CropSquare" -> R.drawable.overlay_recents_square
    "RecentsLinesHorizontal" -> R.drawable.overlay_recents_lines_horizontal
    "RecentsLinesVertical" -> R.drawable.overlay_recents_lines_vertical
    "Layers" -> R.drawable.overlay_recents_layers
    "DashboardCustomize" -> R.drawable.overlay_recents_grid
    "Apps" -> R.drawable.overlay_recents_classic
    "ViewAgenda" -> R.drawable.overlay_recents_outlined
    else -> null
}
