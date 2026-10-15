package com.gamegesture.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.gamegesture.app.MainActivity
import com.gamegesture.app.R
import com.gamegesture.app.data.model.ShortcutConfig
import com.gamegesture.app.data.repository.GestureRepository
import com.gamegesture.app.data.repository.SettingsRepository
import com.gamegesture.app.utils.PermissionUtils
import com.gamegesture.app.utils.ScreenUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.hypot

class FloatingShortcutService : Service() {

    companion object {
        const val ACTION_START = "com.gamegesture.app.action.START_OVERLAY"
        const val ACTION_STOP = "com.gamegesture.app.action.STOP_OVERLAY"
        const val ACTION_RELOAD = "com.gamegesture.app.action.RELOAD_CONFIG"

        private const val NOTIFICATION_ID = 9001
        private const val CHANNEL_ID = "game_gesture_overlay_channel"

        private val _isOverlayRunning = MutableStateFlow(false)
        val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, FloatingShortcutService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingShortcutService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingView: FloatingButtonView? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var gestureRepository: GestureRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        gestureRepository = GestureRepository(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                removeOverlay()
                stopForeground(true)
                stopSelf()
                _isOverlayRunning.value = false
                return START_NOT_STICKY
            }
            ACTION_RELOAD -> {
                floatingView?.invalidate()
            }
            else -> {
                if (!PermissionUtils.hasOverlayPermission(this)) {
                    Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_LONG).show()
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForeground(NOTIFICATION_ID, buildNotification())
                showOverlay()
                _isOverlayRunning.value = true
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        _isOverlayRunning.value = false
    }

    private fun showOverlay() {
        if (floatingView != null) return

        val config = settingsRepository.shortcutConfig.value
        val metrics = ScreenUtils.getScreenMetrics(this)

        val sizePx = (config.sizeDp * metrics.density).toInt().coerceIn(80, 240)
        val initialX = (config.normalizedPositionX * metrics.widthPixels).toInt() - sizePx / 2
        val initialY = (config.normalizedPositionY * metrics.heightPixels).toInt() - sizePx / 2

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX.coerceIn(0, metrics.widthPixels - sizePx)
            y = initialY.coerceIn(0, metrics.heightPixels - sizePx)
        }

        floatingView = FloatingButtonView(this)
        windowManager?.addView(floatingView, windowParams)
    }

    private fun removeOverlay() {
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingShortcutService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GameGesture Engine Active")
            .setContentText("Tap overlay to trigger gestures. Press Stop to close.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop Overlay", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GameGesture Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status of the floating gaming shortcut button"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ViewConstructor")
    inner class FloatingButtonView(context: Context) : View(context) {

        private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var initialX = 0
        private var initialY = 0
        private var isDragging = false

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val config = settingsRepository.shortcutConfig.value
            val primaryColor = try {
                Color.parseColor(config.colorHex)
            } catch (e: Exception) {
                Color.parseColor("#00E5FF")
            }

            val alphaInt = (config.opacity * 255).toInt().coerceIn(40, 255)
            val cx = width / 2f
            val cy = height / 2f
            val r = (width / 2f) - 8f

            // Translucent dark-tint background
            bgPaint.color = Color.argb((alphaInt * 0.75f).toInt(), 13, 15, 20)
            canvas.drawCircle(cx, cy, r, bgPaint)

            // Neon glowing stroke ring
            ringPaint.color = primaryColor
            ringPaint.alpha = alphaInt
            canvas.drawCircle(cx, cy, r, ringPaint)

            // Inner crosshair / gaming symbol
            iconPaint.color = primaryColor
            iconPaint.alpha = alphaInt
            val arm = r * 0.45f
            canvas.drawLine(cx - arm, cy, cx + arm, cy, iconPaint)
            canvas.drawLine(cx, cy - arm, cx, cy + arm, iconPaint)
            canvas.drawCircle(cx, cy, arm * 0.4f, iconPaint)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val params = windowParams ?: return super.onTouchEvent(event)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY

                    if (!isDragging && hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        val config = settingsRepository.shortcutConfig.value
                        if (!config.isLocked) {
                            isDragging = true
                        }
                    }

                    if (isDragging) {
                        params.x = (initialX + dx).toInt()
                        params.y = (initialY + dy).toInt()
                        windowManager?.updateViewLayout(this, params)
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Click event triggered!
                        onButtonClicked()
                    } else {
                        // Drag completed: save normalized position
                        val metrics = ScreenUtils.getScreenMetrics(context)
                        val normX = (params.x + width / 2f) / metrics.widthPixels
                        val normY = (params.y + height / 2f) / metrics.heightPixels
                        val current = settingsRepository.shortcutConfig.value
                        settingsRepository.updateShortcutConfig(
                            current.copy(
                                normalizedPositionX = normX.coerceIn(0.05f, 0.95f),
                                normalizedPositionY = normY.coerceIn(0.05f, 0.95f)
                            )
                        )
                    }
                    isDragging = false
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        private fun onButtonClicked() {
            val shortcut = settingsRepository.shortcutConfig.value
            val gesture = gestureRepository.getGestureById(shortcut.gestureSequenceId)
                ?: gestureRepository.gestures.value.firstOrNull()

            if (gesture == null) {
                Toast.makeText(context, "No gesture assigned to shortcut", Toast.LENGTH_SHORT).show()
                return
            }

            val accessibilityService = GestureAccessibilityService.instance
            if (accessibilityService == null) {
                Toast.makeText(
                    context,
                    "Accessibility Service disabled. Please enable it in Settings.",
                    Toast.LENGTH_LONG
                ).show()
                PermissionUtils.openAccessibilitySettings(context)
                return
            }

            val settings = settingsRepository.settings.value
            accessibilityService.executeSequence(
                sequence = gesture,
                triggerHaptic = settings.hapticFeedback
            ) { success, message ->
                if (!success && settings.debugMode) {
                    serviceScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Gesture failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
