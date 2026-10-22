package cz.kamlar.androiddatausage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class UsageCalcTest {

    private val prague = ZoneId.of("Europe/Prague")
    private val gb = UsageCalc.GB
    private val limit12 = 12 * gb

    private fun at(y: Int, m: Int, d: Int, h: Int = 10, min: Int = 24) =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, prague)

    @Test
    fun periodStartsOnFirstDayAtMidnight() {
        val start = UsageCalc.periodStart(at(2026, 9, 23))
        assertEquals(at(2026, 9, 1, 0, 0), start)
        assertEquals(at(2026, 10, 1, 0, 0), UsageCalc.periodEnd(at(2026, 9, 23)))
    }

    @Test
    fun periodOnFirstDayIsSameDay() {
        assertEquals(at(2026, 10, 1, 0, 0), UsageCalc.periodStart(at(2026, 10, 1, 0, 5)))
    }

    @Test
    fun periodAcrossYearEnd() {
        assertEquals(at(2027, 1, 1, 0, 0), UsageCalc.periodEnd(at(2026, 12, 31, 23, 59)))
    }

    @Test
    fun previewScenarioMatches() {
        val s = UsageCalc.compute(8_740_000_000L, limit12, at(2026, 9, 23))
        assertEquals(73, s.percent)
        assertEquals(3_260_000_000L, s.remainingBytes)
        assertEquals(0L, s.overBytes)
        assertEquals(8, s.daysLeft)
        assertEquals("0,38", UsageCalc.formatGb(s.avgPerDayBytes, 2))
        assertEquals("11,4", UsageCalc.formatGb(s.estimateBytes, 1))
        assertFalse(s.isWarning)
        assertFalse(s.isOver)
        assertEquals(Level.OK, s.level)
    }

    @Test
    fun overLimit() {
        val s = UsageCalc.compute(12_500_000_000L, limit12, at(2026, 9, 28))
        assertEquals(104, s.percent)
        assertEquals(0L, s.remainingBytes)
        assertEquals(500_000_000L, s.overBytes)
        assertTrue(s.isOver)
        assertTrue(s.isWarning)
        assertEquals(Level.OVER, s.level)
    }

    @Test
    fun warningFromNinetyPercent() {
        val warn = UsageCalc.compute(10_800_000_000L, limit12, at(2026, 9, 20))
        assertTrue(warn.isWarning)
        assertEquals(Level.WARN, warn.level)
        assertFalse(UsageCalc.compute(10_700_000_000L, limit12, at(2026, 9, 20)).isWarning)
    }

    @Test
    fun zeroUsageAndNegativeInput() {
        val s = UsageCalc.compute(-5, limit12, at(2026, 9, 1, 0, 1))
        assertEquals(0, s.percent)
        assertEquals(limit12, s.remainingBytes)
        assertEquals(0L, s.estimateBytes)
        assertEquals(30, s.daysLeft)
    }

    @Test
    fun zeroLimitDoesNotDivideByZero() {
        assertEquals(0, UsageCalc.compute(1_000, 0, at(2026, 9, 5)).percent)
    }

    @Test
    fun februaryAndLeapYear() {
        assertEquals(1, UsageCalc.compute(0, limit12, at(2027, 2, 28)).daysLeft)
        assertEquals(2, UsageCalc.compute(0, limit12, at(2028, 2, 28)).daysLeft)
        val s = UsageCalc.compute(2_800_000_000L, limit12, at(2027, 2, 14))
        assertEquals("5,6", UsageCalc.formatGb(s.estimateBytes, 1))
    }

    @Test
    fun lastDayOfMonth() {
        assertEquals(1, UsageCalc.compute(0, limit12, at(2026, 8, 31)).daysLeft)
    }

    @Test
    fun samsungScreenshotMatchesDecimalUnits() {
        // 24. 9.: Samsung ukazoval 9,71 GB; aplikace se zaškrtnutou volbou 1024 MB ukazovala 9,05.
        assertEquals("9,72", UsageCalc.formatGb(9_717_363_507L, 2))
    }

    @Test
    fun formatLimit() {
        assertEquals("12", UsageCalc.formatLimit(12.0))
        assertEquals("12,5", UsageCalc.formatLimit(12.5))
    }

    @Test
    fun parseLimit() {
        assertEquals(12.0, UsageCalc.parseLimit("12")!!, 0.0)
        assertEquals(12.5, UsageCalc.parseLimit(" 12,5 ")!!, 0.0)
        assertEquals(12.5, UsageCalc.parseLimit("12.5")!!, 0.0)
        assertNull(UsageCalc.parseLimit(""))
        assertNull(UsageCalc.parseLimit("0"))
        assertNull(UsageCalc.parseLimit("-3"))
        assertNull(UsageCalc.parseLimit("abc"))
    }

    @Test
    fun notificationTextsInCzech() {
        val s = UsageCalc.compute(8_740_000_000L, limit12, at(2026, 9, 23))
        assertEquals("Mobilní data: 8,74 GB z 12 GB", UsageCalc.title(s, 12.0))
        assertEquals(
            "73 % · zbývá 3,26 GB · do konce období 8 dní\n" +
                "Ø 0,38 GB/den · odhad na konci měsíce 11,4 GB",
            UsageCalc.body(s),
        )
        val over = UsageCalc.compute(12_500_000_000L, limit12, at(2026, 9, 28))
        assertTrue(UsageCalc.body(over).contains("překročeno o 0,50 GB"))
        assertTrue(UsageCalc.body(over).contains("do konce období 3 dny"))
    }

    @Test
    fun mainScreenRedesignScenario() {
        // Náhled nového rozložení: 22. 10., 8,40 GB z 12 GB.
        val now = at(2026, 10, 22, 8, 42)
        val s = UsageCalc.compute(8_400_000_000L, limit12, now)
        assertEquals(70, s.percent)
        assertEquals("8,40 z 12 GB", UsageCalc.usageOfLimit(s, 12.0))
        assertEquals(
            listOf(
                Tile("Zbývá", "3,60 GB"),
                Tile("Do konce období", "10 dní"),
                Tile("Ø za den", "0,38 GB"),
                Tile("Odhad na konci", "11,8 GB"),
            ),
            UsageCalc.tiles(s),
        )
        assertEquals("1. 10." to "31. 10.", UsageCalc.periodLabels(now))
        assertEquals("aktualizováno 8:42", UsageCalc.updatedLabel(now))
    }

    @Test
    fun tilesWarnWhenOverOrEstimateOverLimit() {
        val over = UsageCalc.tiles(UsageCalc.compute(12_500_000_000L, limit12, at(2026, 9, 28)))
        assertEquals(Tile("Překročeno o", "0,50 GB", warn = true), over[0])
        assertTrue(over[3].warn)

        val trending = UsageCalc.compute(6_000_000_000L, limit12, at(2026, 9, 10))
        assertTrue(trending.estimateOverLimit)
        assertTrue(UsageCalc.tiles(trending)[3].warn)
        assertFalse(UsageCalc.tiles(trending)[0].warn)
    }

    @Test
    fun periodLabelsInFebruary() {
        assertEquals("1. 2." to "28. 2.", UsageCalc.periodLabels(at(2027, 2, 14)))
        assertEquals("1. 2." to "29. 2.", UsageCalc.periodLabels(at(2028, 2, 14)))
    }

    @Test
    fun stepsLeft() {
        assertEquals("Zbývá 1 krok", UsageCalc.stepsLeft(1))
        assertEquals("Zbývají 2 kroky", UsageCalc.stepsLeft(2))
        assertEquals("Zbývají 3 kroky", UsageCalc.stepsLeft(3))
    }

    @Test
    fun daysWord() {
        assertEquals("den", UsageCalc.daysWord(1))
        assertEquals("dny", UsageCalc.daysWord(4))
        assertEquals("dní", UsageCalc.daysWord(5))
        assertEquals("dní", UsageCalc.daysWord(0))
    }
}
