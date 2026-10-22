package cz.kamlar.androiddatausage

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/** Barevná úroveň spotřeby — sdílí ji notifikace i hlavní obrazovka. */
enum class Level(val argb: Int) {
    OK(0xFF3D8BFD.toInt()),
    WARN(0xFFF59E0B.toInt()),
    OVER(0xFFE5484D.toInt()),
}

/** Spočítané hodnoty za aktuální zúčtovací období (vždy od 1. dne v měsíci). */
data class UsageStats(
    val usedBytes: Long,
    val limitBytes: Long,
    val percent: Int,
    val remainingBytes: Long,
    val overBytes: Long,
    val daysLeft: Int,
    val avgPerDayBytes: Long,
    val estimateBytes: Long,
) {
    val isOver: Boolean get() = overBytes > 0
    val isWarning: Boolean get() = percent >= 90
    val estimateOverLimit: Boolean get() = estimateBytes > limitBytes
    val level: Level get() = when {
        isOver -> Level.OVER
        isWarning -> Level.WARN
        else -> Level.OK
    }
}

/** Dlaždice na hlavní obrazovce: popisek, hodnota a zda ji zvýraznit varovnou barvou. */
data class Tile(val label: String, val value: String, val warn: Boolean = false)

/**
 * Čistá logika bez závislosti na Android API — pokrytá unit testy.
 * Jednotky jsou vždy desítkové (1 GB = 1000³ B), stejně jako Nastavení → Využití dat na Samsungu.
 */
object UsageCalc {

    const val GB = 1_000_000_000L

    private val CS = Locale.forLanguageTag("cs-CZ")
    private val TIME = DateTimeFormatter.ofPattern("H:mm")

    /** Začátek zúčtovacího období: 1. den aktuálního měsíce 00:00 v zóně [now]. */
    fun periodStart(now: ZonedDateTime): ZonedDateTime =
        now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)

    fun periodEnd(now: ZonedDateTime): ZonedDateTime = periodStart(now).plusMonths(1)

    /**
     * Dnešek se počítá jako celý den — průměr i odhad jsou tak konzervativní
     * a ráno 1. dne v měsíci nevychází absurdně vysoký odhad.
     */
    fun compute(usedBytes: Long, limitBytes: Long, now: ZonedDateTime): UsageStats {
        val used = usedBytes.coerceAtLeast(0)
        val daysInMonth = now.toLocalDate().lengthOfMonth()
        val dayOfMonth = now.dayOfMonth
        val percent = if (limitBytes > 0) (used.toDouble() / limitBytes * 100).roundToInt() else 0
        return UsageStats(
            usedBytes = used,
            limitBytes = limitBytes,
            percent = percent,
            remainingBytes = (limitBytes - used).coerceAtLeast(0),
            overBytes = (used - limitBytes).coerceAtLeast(0),
            daysLeft = daysInMonth - dayOfMonth + 1,
            avgPerDayBytes = used / dayOfMonth,
            estimateBytes = used.toDouble().div(dayOfMonth).times(daysInMonth).toLong(),
        )
    }

    fun formatGb(bytes: Long, decimals: Int): String =
        String.format(CS, "%.${decimals}f", bytes.toDouble() / GB)

    /** Limit zadaný v GB (např. 12 nebo 12,5) bez zbytečných desetinných míst. */
    fun formatLimit(limitGb: Double): String =
        if (limitGb == Math.floor(limitGb)) limitGb.toLong().toString()
        else String.format(CS, "%.1f", limitGb)

    /** Text z pole limitu ("12", "12,5", "12.5") → GB, nebo null, když není kladné číslo. */
    fun parseLimit(text: String): Double? =
        text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() }

    // --- Notifikace ---

    fun title(s: UsageStats, limitGb: Double): String =
        "Mobilní data: ${formatGb(s.usedBytes, 2)} GB z ${formatLimit(limitGb)} GB"

    fun body(s: UsageStats): String {
        val state = if (s.isOver) "překročeno o ${formatGb(s.overBytes, 2)} GB"
        else "zbývá ${formatGb(s.remainingBytes, 2)} GB"
        return "${s.percent} % · $state · do konce období ${s.daysLeft} ${daysWord(s.daysLeft)}\n" +
            "Ø ${formatGb(s.avgPerDayBytes, 2)} GB/den · " +
            "odhad na konci měsíce ${formatGb(s.estimateBytes, 1)} GB"
    }

    // --- Hlavní obrazovka ---

    /** "8,40 z 12 GB" */
    fun usageOfLimit(s: UsageStats, limitGb: Double): String =
        "${formatGb(s.usedBytes, 2)} z ${formatLimit(limitGb)} GB"

    fun tiles(s: UsageStats): List<Tile> = listOf(
        if (s.isOver) Tile("Překročeno o", "${formatGb(s.overBytes, 2)} GB", warn = true)
        else Tile("Zbývá", "${formatGb(s.remainingBytes, 2)} GB"),
        Tile("Do konce období", "${s.daysLeft} ${daysWord(s.daysLeft)}"),
        Tile("Ø za den", "${formatGb(s.avgPerDayBytes, 2)} GB"),
        Tile("Odhad na konci", "${formatGb(s.estimateBytes, 1)} GB", warn = s.estimateOverLimit),
    )

    /** Popisky pod proužkem: první a poslední den období, např. "1. 10." a "31. 10.". */
    fun periodLabels(now: ZonedDateTime): Pair<String, String> {
        val start = periodStart(now)
        val last = periodEnd(now).minusDays(1)
        return "${start.dayOfMonth}. ${start.monthValue}." to "${last.dayOfMonth}. ${last.monthValue}."
    }

    fun updatedLabel(at: ZonedDateTime): String = "aktualizováno ${at.format(TIME)}"

    /** "Zbývá 1 krok", "Zbývají 2 kroky", ... */
    fun stepsLeft(n: Int): String = when (n) {
        1 -> "Zbývá 1 krok"
        in 2..4 -> "Zbývají $n kroky"
        else -> "Zbývá $n kroků"
    }

    fun daysWord(n: Int): String = when (n) {
        1 -> "den"
        in 2..4 -> "dny"
        else -> "dní"
    }
}
