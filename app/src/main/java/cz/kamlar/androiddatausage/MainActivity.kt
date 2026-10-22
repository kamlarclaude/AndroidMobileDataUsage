package cz.kamlar.androiddatausage

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.time.ZonedDateTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Hlavní obrazovka (UI skládané v kódu, jen framework komponenty):
 * velká procenta + proužek, dlaždice 2×2, přepínač zobrazení na liště, limit (ukládá se sám)
 * a oprávnění — nahoře jako "Dokončete nastavení", když něco chybí, jinak sbalená dole.
 */
class MainActivity : Activity() {

    private class Perm(
        val title: String,
        val subtitle: String?,
        val granted: () -> Boolean,
        val request: () -> Unit,
    )

    private class PermRow(val perm: Perm, val action: Button, val done: TextView)

    private lateinit var prefs: Prefs
    private val handler = Handler(Looper.getMainLooper())
    private val io: ExecutorService = Executors.newSingleThreadExecutor()

    private var colorPrimary = 0
    private var colorSecondary = 0
    private var colorCard = 0
    private var colorTrack = 0

    private lateinit var content: LinearLayout
    private lateinit var updatedText: TextView
    private lateinit var heroPercent: TextView
    private lateinit var heroUsage: TextView
    private lateinit var heroHint: TextView
    private lateinit var progress: ProgressBar
    private lateinit var periodRow: LinearLayout
    private lateinit var periodStartText: TextView
    private lateinit var periodEndText: TextView
    private lateinit var tilesView: LinearLayout
    private val tileLabels = ArrayList<TextView>()
    private val tileValues = ArrayList<TextView>()
    private lateinit var showSwitch: Switch
    private lateinit var switchSub: TextView
    private lateinit var limitInput: EditText
    private lateinit var permsCard: LinearLayout
    private lateinit var permsTitle: TextView
    private lateinit var permsSub: TextView
    private val permRows = ArrayList<PermRow>()
    private lateinit var permsOkRow: LinearLayout
    private lateinit var permsChevron: TextView
    private var permsExpanded = false

    private val tick = object : Runnable {
        override fun run() {
            refreshUi()
            handler.postDelayed(this, REFRESH_MS)
        }
    }

    private val saveLimit = Runnable { saveLimitNow() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        prefs = Prefs(this)
        resolveColors()
        setContentView(buildLayout())
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        handler.removeCallbacks(tick)
        if (handler.hasCallbacks(saveLimit)) {
            handler.removeCallbacks(saveLimit)
            saveLimitNow()
        }
        super.onPause()
    }

    override fun onDestroy() {
        io.shutdownNow()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshUi()
    }

    // --- Stav ---

    private fun hasNotificationPermission() =
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun isBatteryUnrestricted() =
        getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)

    private fun refreshUi() {
        bindPermissions()
        bindSwitch()
        // NetworkStatsManager je binder volání — mimo hlavní vlákno, výsledek zpět do UI.
        io.execute {
            val now = ZonedDateTime.now()
            val used = UsageReader.mobileBytesThisPeriod(this, now)
            handler.post { if (!isDestroyed) bindUsage(now, used) }
        }
    }

    private fun bindUsage(now: ZonedDateTime, used: Long?) {
        if (used == null) {
            heroPercent.text = "– %"
            heroPercent.setTextColor(colorSecondary)
            heroUsage.text = "? z ${UsageCalc.formatLimit(prefs.limitGb)} GB"
            progress.progress = 0
            heroHint.visibility = View.VISIBLE
            periodRow.visibility = View.GONE
            tilesView.visibility = View.GONE
            updatedText.text = ""
            return
        }
        val s = UsageCalc.compute(used, prefs.limitBytes, now)
        heroPercent.text = "${s.percent} %"
        heroPercent.setTextColor(if (s.level == Level.OK) colorPrimary else s.level.argb)
        heroUsage.text = UsageCalc.usageOfLimit(s, prefs.limitGb)
        progress.progress = s.percent.coerceAtMost(100)
        progress.progressTintList = ColorStateList.valueOf(s.level.argb)
        heroHint.visibility = View.GONE

        val (first, last) = UsageCalc.periodLabels(now)
        periodStartText.text = first
        periodEndText.text = last
        periodRow.visibility = View.VISIBLE

        UsageCalc.tiles(s).forEachIndexed { i, t ->
            tileLabels[i].text = t.label
            tileValues[i].text = t.value
            tileValues[i].setTextColor(if (t.warn) Level.WARN.argb else colorPrimary)
        }
        tilesView.visibility = View.VISIBLE
        updatedText.text = UsageCalc.updatedLabel(now)
    }

    private fun bindPermissions() {
        var missing = 0
        for (row in permRows) {
            val ok = row.perm.granted()
            if (!ok) missing++
            row.action.visibility = if (ok) View.GONE else View.VISIBLE
            row.done.visibility = if (ok) View.VISIBLE else View.GONE
        }

        (permsCard.parent as? ViewGroup)?.removeView(permsCard)
        val bg = permsCard.background as GradientDrawable
        if (missing > 0) {
            permsTitle.text = "Dokončete nastavení"
            permsSub.text = UsageCalc.stepsLeft(missing)
            bg.setStroke(dp(1), Level.WARN.argb)
            permsOkRow.visibility = View.GONE
            content.addView(permsCard, 1) // hned pod nadpis
        } else {
            permsTitle.text = "Oprávnění"
            permsSub.text = "Vše povoleno"
            bg.setStroke(0, Color.TRANSPARENT)
            permsOkRow.visibility = View.VISIBLE
            permsChevron.text = if (permsExpanded) "▴" else "▾"
            if (permsExpanded) content.addView(permsCard)
        }
    }

    private fun bindSwitch() {
        val notifOk = hasNotificationPermission()
        showSwitch.setOnCheckedChangeListener(null)
        showSwitch.isChecked = prefs.enabled
        showSwitch.setOnCheckedChangeListener { _, checked -> setShowing(checked) }
        // Vypnout jde vždy; zapnout jen s povolenými notifikacemi.
        showSwitch.isEnabled = notifOk || prefs.enabled
        switchSub.text = if (!notifOk && !prefs.enabled) "Nejdřív povolte notifikace" else "Procenta vedle hodin"
    }

    private fun setShowing(on: Boolean) {
        prefs.enabled = on
        if (on) DataUsageService.start(this) else DataUsageService.stop(this)
        bindSwitch()
    }

    private fun saveLimitNow() {
        val v = UsageCalc.parseLimit(limitInput.text.toString())
        if (v == null) {
            limitInput.error = "Zadejte kladné číslo, třeba 12"
            return
        }
        if (v == prefs.limitGb) return
        prefs.limitGb = v
        if (prefs.enabled) DataUsageService.start(this) // přepočítá notifikaci hned
        refreshUi()
    }

    // --- Akce oprávnění ---

    private fun requestUsageAccess() {
        try {
            // Rovnou stránka této aplikace — systém to podporovat nemusí.
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:$packageName")))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Najděte „${getString(R.string.app_name)}“ a zapněte přístup.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun requestNotifications() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (prefs.notificationAsked && !shouldShowRequestPermissionRationale(permission)) {
            // Po dvojím zamítnutí Android dialog už nezobrazí — otevřít nastavení notifikací aplikace.
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
            )
        } else {
            prefs.notificationAsked = true
            requestPermissions(arrayOf(permission), 1)
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestBattery() {
        startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
    }

    // --- Rozložení ---

    private fun buildLayout(): View {
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        content.addView(buildHeader())
        content.addView(buildHero())
        content.addView(buildTiles())
        content.addView(buildSwitchCard())
        content.addView(buildLimitCard())
        content.addView(buildPermsOkRow())
        permsCard = buildPermsCard() // umisťuje bindPermissions()

        // Android 15+ (targetSdk 35) kreslí až pod systémové lišty — odsadit obsah o jejich výšku.
        val pad = dp(16)
        content.setOnApplyWindowInsetsListener { v, insets ->
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            v.setPadding(pad + bars.left, pad + bars.top, pad + bars.right, pad + bars.bottom)
            insets
        }
        return ScrollView(this).apply { addView(content) }
    }

    private fun buildHeader() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(4), dp(8), dp(4), dp(4))
        addView(text(getString(R.string.app_name), 22f, colorPrimary, medium = true), weighted())
        updatedText = text("", 12f, colorSecondary)
        addView(updatedText)
    }

    private fun buildHero() = card().apply {
        setPadding(dp(16), dp(18), dp(16), dp(14))
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
            heroPercent = text("– %", 46f, colorPrimary, medium = true).apply { includeFontPadding = false }
            heroUsage = text("", 15f, colorSecondary).apply { setPadding(dp(12), 0, 0, dp(4)) }
            addView(heroPercent)
            addView(heroUsage)
        })
        progress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            minHeight = dp(8)
            maxHeight = dp(8)
            progressBackgroundTintList = ColorStateList.valueOf(colorTrack)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO // procenta čte TextView
        }
        addView(progress, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)).apply {
            topMargin = dp(14)
        })
        periodRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, 0)
            periodStartText = text("", 12f, colorSecondary)
            periodEndText = text("", 12f, colorSecondary).apply { gravity = Gravity.END }
            addView(periodStartText, weighted())
            addView(periodEndText, weighted())
        }
        addView(periodRow)
        heroHint = text("Spotřeba se zobrazí po povolení přístupu k údajům o využití.", 13f, colorSecondary).apply {
            setPadding(0, dp(8), 0, 0)
            visibility = View.GONE
        }
        addView(heroHint)
    }

    private fun buildTiles() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        tilesView = this
        for (r in 0 until 2) {
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                for (c in 0 until 2) {
                    addView(buildTile(), weighted().apply {
                        topMargin = dp(8)
                        if (c == 0) marginEnd = dp(4) else marginStart = dp(4)
                    })
                }
            })
        }
    }

    private fun buildTile() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(colorCard, 14)
        setPadding(dp(12), dp(10), dp(12), dp(10))
        val label = text("", 12f, colorSecondary)
        val value = text("", 16f, colorPrimary, medium = true)
        tileLabels.add(label)
        tileValues.add(value)
        addView(label)
        addView(value)
    }

    private fun buildSwitchCard() = card().apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        switchSub = text("", 13f, colorSecondary)
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(text("Zobrazovat na liště", 16f, colorPrimary, medium = true))
            addView(switchSub)
        }
        showSwitch = Switch(context).apply { contentDescription = "Zobrazovat na liště" }
        addView(texts, weighted())
        addView(showSwitch)
        setOnClickListener { if (showSwitch.isEnabled) showSwitch.toggle() }
    }

    private fun buildLimitCard() = card().apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        limitInput = EditText(context).apply {
            id = View.generateViewId()
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            imeOptions = EditorInfo.IME_ACTION_DONE
            gravity = Gravity.END
            textSize = 18f
            setText(UsageCalc.formatLimit(prefs.limitGb))
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handler.removeCallbacks(saveLimit)
                    saveLimitNow()
                    v.clearFocus()
                    getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(v.windowToken, 0)
                }
                false
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    limitInput.error = null
                    handler.removeCallbacks(saveLimit)
                    handler.postDelayed(saveLimit, SAVE_DELAY_MS)
                }
            })
        }
        val label = text("Měsíční limit", 16f, colorPrimary, medium = true).apply { labelFor = limitInput.id }
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(label)
            addView(text("Ukládá se automaticky", 13f, colorSecondary))
        }
        addView(texts, weighted())
        addView(limitInput, LinearLayout.LayoutParams(dp(72), ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(text("GB", 16f, colorSecondary).apply { setPadding(dp(6), 0, 0, 0) })
    }

    private fun buildPermsOkRow() = card().apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(12), dp(16), dp(12))
        addView(text("✓ Všechna oprávnění povolena", 14f, COLOR_DONE), weighted())
        permsChevron = text("▾", 16f, colorSecondary)
        addView(permsChevron)
        setOnClickListener {
            permsExpanded = !permsExpanded
            bindPermissions()
        }
        permsOkRow = this
    }

    private fun buildPermsCard() = card().apply {
        permsTitle = text("", 16f, colorPrimary, medium = true)
        permsSub = text("", 13f, colorSecondary).apply { setPadding(0, 0, 0, dp(6)) }
        addView(permsTitle)
        addView(permsSub)
        val perms = listOf(
            Perm("Přístup k údajům o využití", "Bez něj nejde spotřebu přečíst",
                { UsageReader.hasUsageAccess(this@MainActivity) }, ::requestUsageAccess),
            Perm("Notifikace", "Pro zobrazení na liště", ::hasNotificationPermission, ::requestNotifications),
            Perm("Baterie bez omezení", "Jinak One UI službu uspává", ::isBatteryUnrestricted, ::requestBattery),
        )
        perms.forEachIndexed { i, p -> addView(buildPermRow(i + 1, p)) }
    }

    private fun buildPermRow(n: Int, p: Perm) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(10), 0, dp(10))
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(text("$n. ${p.title}", 15f, colorPrimary))
            p.subtitle?.let { addView(text(it, 12f, colorSecondary)) }
        }
        val action = Button(context).apply {
            text = "Povolit"
            isAllCaps = false
            textSize = 14f
            setTextColor(Color.WHITE)
            background = rounded(Level.OK.argb, 18)
            minHeight = dp(36)
            minimumHeight = dp(36)
            minWidth = 0
            minimumWidth = 0
            setPadding(dp(16), 0, dp(16), 0)
            stateListAnimator = null
            contentDescription = "Povolit: ${p.title}"
            setOnClickListener { p.request() }
        }
        val done = text("✓ Hotovo", 14f, COLOR_DONE)
        addView(texts, weighted())
        addView(action, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(36)))
        addView(done)
        permRows.add(PermRow(p, action, done))
    }

    // --- Pomocné ---

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(colorCard, 18)
        setPadding(dp(16), dp(14), dp(16), dp(14))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = dp(10) }
    }

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun text(value: String, sizeSp: Float, color: Int, medium: Boolean = false) = TextView(this).apply {
        text = value
        textSize = sizeSp
        setTextColor(color)
        if (medium) typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private fun weighted() = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    /** Barvy ze systémového motivu — karty jsou jemný závoj barvy textu, takže fungují ve světlém i tmavém režimu. */
    private fun resolveColors() {
        colorPrimary = attrColor(android.R.attr.textColorPrimary, Color.BLACK)
        colorSecondary = attrColor(android.R.attr.textColorSecondary, Color.GRAY)
        val rgb = colorPrimary and 0x00FFFFFF
        colorCard = rgb or 0x14000000   // ~8 %
        colorTrack = rgb or 0x29000000  // ~16 %
    }

    private fun attrColor(attr: Int, fallback: Int): Int {
        val a = obtainStyledAttributes(intArrayOf(attr))
        return try { a.getColor(0, fallback) } finally { a.recycle() }
    }

    private companion object {
        const val REFRESH_MS = 60_000L
        const val SAVE_DELAY_MS = 600L
        const val COLOR_DONE = 0xFF34A853.toInt()
    }
}
