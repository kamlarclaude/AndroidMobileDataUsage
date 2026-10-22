package cz.kamlar.androiddatausage

import android.content.Context

/** Uživatelské nastavení v SharedPreferences. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var limitGb: Double
        get() = sp.getFloat(KEY_LIMIT, DEFAULT_LIMIT_GB).toDouble()
        set(v) = sp.edit().putFloat(KEY_LIMIT, v.toFloat()).apply()

    /** Uživatel službu zapnul — používá BootReceiver po restartu telefonu. */
    var enabled: Boolean
        get() = sp.getBoolean(KEY_ENABLED, false)
        set(v) = sp.edit().putBoolean(KEY_ENABLED, v).apply()

    /**
     * Už jsme o notifikace jednou žádali. Po dvojím zamítnutí Android dialog nezobrazí,
     * takže MainActivity podle tohohle pozná, že má otevřít systémové nastavení.
     */
    var notificationAsked: Boolean
        get() = sp.getBoolean(KEY_NOTIF_ASKED, false)
        set(v) = sp.edit().putBoolean(KEY_NOTIF_ASKED, v).apply()

    val limitBytes: Long
        get() = (limitGb * UsageCalc.GB).toLong()

    private companion object {
        const val KEY_LIMIT = "limitGb"
        const val KEY_ENABLED = "enabled"
        const val KEY_NOTIF_ASKED = "notificationAsked"
        const val DEFAULT_LIMIT_GB = 12f
        // Klíč "binaryUnits" (zrušená volba 1 GB = 1024 MB) se už nečte.
    }
}
