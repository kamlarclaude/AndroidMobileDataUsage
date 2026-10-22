package cz.kamlar.androiddatausage

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import java.time.ZonedDateTime

/**
 * Služba na popředí, která drží notifikaci s procenty ve stavovém řádku
 * a obnovuje ji každou minutu. Při zhasnutém displeji neobnovuje (šetří baterii).
 */
class DataUsageService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs

    private val tick = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, UPDATE_INTERVAL_MS)
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handler.removeCallbacks(tick)
            if (intent.action == Intent.ACTION_SCREEN_ON) handler.post(tick)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        createChannel()
        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }, RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        handler.removeCallbacks(tick)
        if (getSystemService(PowerManager::class.java).isInteractive) {
            handler.postDelayed(tick, UPDATE_INTERVAL_MS)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        unregisterReceiver(screenReceiver)
        super.onDestroy()
    }

    private fun refresh() {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createChannel() {
        // IMPORTANCE_DEFAULT (ne LOW): tiché notifikace může One UI ve stavovém řádku skrýt.
        // Zvuk a vibrace jsou vypnuté, takže nic neruší.
        val channel = NotificationChannel(CHANNEL_ID, "Spotřeba mobilních dat", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Trvalá notifikace s aktuální spotřebou mobilních dat"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_STATUS)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)

        val now = ZonedDateTime.now()
        val used = UsageReader.mobileBytesThisPeriod(this, now)
        if (used == null) {
            return builder
                .setSmallIcon(IconRenderer.percentIcon(this, "?", ""))
                .setContentTitle("Chybí přístup k údajům o využití")
                .setContentText("Klepněte a povolte ho v aplikaci.")
                .build()
        }

        val s = UsageCalc.compute(used, prefs.limitBytes, now)
        val body = UsageCalc.body(s)
        return builder
            .setSmallIcon(IconRenderer.percentIcon(this, s.percent.toString()))
            .setContentTitle(UsageCalc.title(s, prefs.limitGb))
            .setContentText(body.lineSequence().first())
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setProgress(100, s.percent.coerceAtMost(100), false)
            .setColor(s.level.argb)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "data_usage"
        private const val NOTIFICATION_ID = 1
        private const val UPDATE_INTERVAL_MS = 60_000L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, DataUsageService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DataUsageService::class.java))
        }
    }
}
