package cz.kamlar.androiddatausage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Icon

/**
 * Ikona do stavového řádku: velké číslo přes celou výšku a malé "%" vpravo nahoře (jako exponent).
 * Systém bere jen alfa kanál a sám ji přebarví (bílá/černá) — barvy tu nemají smysl.
 */
object IconRenderer {

    private const val ICON_DP = 24f
    private const val NUMBER_HEIGHT = 0.80f
    private const val SUFFIX_HEIGHT = 0.42f

    fun percentIcon(context: Context, number: String, suffix: String = "%"): Icon {
        val size = (ICON_DP * context.resources.displayMetrics.density).toInt()
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val big = paint(size * NUMBER_HEIGHT)
        val small = paint(size * SUFFIX_HEIGHT)

        // Zúžit obojí stejným poměrem, pokud se nevejde na šířku (např. "104%").
        val total = big.measureText(number) + small.measureText(suffix)
        if (total > size) {
            val k = size / total
            big.textSize *= k
            small.textSize *= k
        }
        val wBig = big.measureText(number)
        val wSmall = small.measureText(suffix)
        val x = (size - wBig - wSmall) / 2f

        // Číslo svisle na střed podle skutečných obrysů číslic.
        val b = Rect()
        big.getTextBounds(number, 0, number.length, b)
        val baseline = size / 2f - (b.top + b.bottom) / 2f
        canvas.drawText(number, x, baseline, big)

        if (suffix.isNotEmpty()) {
            // Horní hrana "%" zarovnaná s horní hranou číslic.
            val s = Rect()
            small.getTextBounds(suffix, 0, suffix.length, s)
            canvas.drawText(suffix, x + wBig, baseline + b.top - s.top, small)
        }
        return Icon.createWithBitmap(bmp)
    }

    private fun paint(textSize: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        this.textSize = textSize
    }
}
