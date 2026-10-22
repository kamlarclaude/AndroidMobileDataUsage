package cz.kamlar.androiddatausage

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Process
import java.time.ZonedDateTime

/** Čte spotřebu mobilních dat, kterou měří sám telefon (NetworkStatsManager). */
object UsageReader {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Přijaté + odeslané bajty přes mobilní síť od 1. dne v měsíci do teď,
     * nebo null, když chybí přístup k údajům o využití.
     * subscriberId = null → souhrn za všechny SIM (od Androidu 10 jediná možnost bez oprávnění operátora).
     */
    @Suppress("DEPRECATION")
    fun mobileBytesThisPeriod(context: Context, now: ZonedDateTime = ZonedDateTime.now()): Long? {
        if (!hasUsageAccess(context)) return null
        val nsm = context.getSystemService(NetworkStatsManager::class.java)
        return try {
            val bucket = nsm.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                null,
                UsageCalc.periodStart(now).toInstant().toEpochMilli(),
                now.toInstant().toEpochMilli(),
            )
            bucket.rxBytes + bucket.txBytes
        } catch (e: SecurityException) {
            null
        }
    }
}
