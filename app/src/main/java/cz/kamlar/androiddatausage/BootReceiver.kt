package cz.kamlar.androiddatausage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Po restartu telefonu (nebo aktualizaci aplikace) znovu spustí službu, pokud byla zapnutá. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED ->
                if (Prefs(context).enabled) DataUsageService.start(context)
        }
    }
}
