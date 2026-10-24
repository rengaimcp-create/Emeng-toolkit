package com.emeng.injector

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.Context
import android.graphics.Typeface
import kotlin.math.hypot
import kotlin.random.Random

class MainActivity : Activity() {
    private val bg = Color.rgb(16, 21, 29)
    private val card = Color.rgb(27, 35, 46)
    private val mint = Color.rgb(104, 240, 192)
    private val prefs by lazy { getSharedPreferences("emeng_settings", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun label(text: String, size: Float = 15f, color: Int = Color.WHITE): TextView =
        TextView(this).apply {
            this.text = text; textSize = size; setTextColor(color)
        }
    private fun button(text: String, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            setTextColor(Color.BLACK)
            setBackgroundColor(mint)
            setOnClickListener { action() }
        }
    private fun base(title: String): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        outer.addView(label("EMENG INJECTOR", 12f, mint))
        val heading = label(title, 27f)
        heading.setTypeface(null, Typeface.BOLD)
        val lp = LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(8); lp.bottomMargin = dp(16)
        outer.addView(heading, lp)
        setContentView(ScrollView(this).apply { addView(outer) })
        return outer
    }
    private fun panel(parent: LinearLayout, title: String, desc: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(card)
        }
        box.addView(label(title, 17f, mint))
        val d = label(desc, 13f, Color.LTGRAY)
        val lp = LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(6)
        box.addView(d, lp)
        val p = LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(12)
        parent.addView(box, p)
    }
    private fun showHome() {
        val root = base("Game Training Toolkit")
        panel(root, "Status: Standalone mode", "Fitur latihan berjalan di aplikasi ini dan tidak mengubah Free Fire.")
        root.addView(button("Crosshair Studio") { showCrosshair() })
        root.addView(button("Training Arena — AIMLOCK demo") { showArena() })
        root.addView(button("ESP demo — target simulasi") { showEspDemo() })
        root.addView(button("Sensitivity Presets") { showSensitivity() })
        root.addView(button("Tentang & batasan") {
            showInfo()
        })
        root.addView(label("Versi 1.0 • Pengaturan tersimpan lokal", 12f, Color.GRAY))
    }
    private fun showInfo() {
        val root = base("Tentang aplikasi")
        panel(root, "Apa yang bekerja", "Crosshair lokal, preset sensitivitas tersimpan, target dummy bergerak, serta visualisasi ESP untuk target simulasi.")
        panel(root, "Batasan", "Aplikasi ini bukan injector untuk Free Fire. Ia tidak membaca memori game, tidak mengunci bidikan ke pemain nyata, tidak menampilkan pemain melalui tembok, dan tidak membypass anti-cheat.")
        root.addView(button("Kembali") { showHome() })
    }
    private fun showCrosshair() {
        val root = base("Crosshair Studio")
        panel(root, "Preview", "Crosshair ini ditampilkan di layar aplikasi, bukan di atas game pihak ketiga.")
        val preview = object : View(this) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mint; strokeWidth = dp(3).toFloat() }
            override fun onDraw(c: Canvas) {
                super.onDraw(c)
                val x = width / 2f; val y = height / 2f
                val gap = dp(prefs.getInt("gap", 8)).toFloat()
                val arm = dp(prefs.getInt("arm", 18)).toFloat()
                c.drawLine(x-gap-arm, y, x-gap, y, p); c.drawLine(x+gap, y, x+gap+arm, y, p)
                c.drawLine(x, y-gap-arm, x, y-gap, p); c.drawLine(x, y+gap, x, y+gap+arm, p)
            }
        }.apply { setBackgroundColor(Color.rgb(8, 11, 16)) }
        root.addView(preview, LinearLayout.LayoutParams(-1, dp(180)))
        fun seek(title: String, key: String, max: Int, initial: Int) {
            root.addView(label(title, 14f))
            val s = SeekBar(this).apply {
                this.max = max
                progress = prefs.getInt(key, initial)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                        prefs.edit().putInt(key, value).apply(); preview.invalidate()
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })
            }
            root.addView(s)
        }
        seek("Panjang garis", "arm", 40, 18)
        seek("Jarak tengah", "gap", 24, 8)
        root.addView(button("Kembali") { showHome() })
    }
    private fun showSensitivity() {
        val root = base("Sensitivity Presets")
        panel(root, "Catatan", "Preset ini disimpan sebagai catatan dalam aplikasi. Android tidak mengubah sensitivitas Free Fire secara otomatis.")
        val presets = listOf("Stabil" to "General 90 • Red Dot 85 • 2x 78 • 4x 65",
            "Seimbang" to "General 95 • Red Dot 90 • 2x 82 • 4x 70",
            "Cepat" to "General 100 • Red Dot 95 • 2x 88 • 4x 76")
        presets.forEach { (name, values) ->
            root.addView(button("Terapkan preset: $name") {
                prefs.edit().putString("preset_name", name).putString("preset_values", values).apply()
                Toast.makeText(this, "$name disimpan", Toast.LENGTH_SHORT).show()
            })
            root.addView(label(values, 13f, Color.LTGRAY))
        }
        root.addView(label("Preset aktif: ${prefs.getString("preset_name", "Belum dipilih")}", 14f, mint))
        root.addView(button("Kembali") { showHome() })
    }
    private fun showArena() {
        val root = base("Training Arena")
        panel(root, "Aim-lock demo", "Sentuh target dummy di arena ini. Ini simulasi latihan lokal, bukan aimlock untuk game lain.")
        val score = label("Skor: 0", 18f, mint)
        root.addView(score)
        val arena = FrameLayout(this).apply { setBackgroundColor(Color.rgb(8, 11, 16)) }
        var points = 0
        fun spawn() {
            arena.removeAllViews()
            val target = TextView(this).apply {
                text = "◎"; textSize = 38f; gravity = Gravity.CENTER; setTextColor(mint)
                setOnClickListener {
                    points++; score.text = "Skor: $points"
                    spawn()
                }
            }
            val size = dp(68)
            val maxX = (resources.displayMetrics.widthPixels - dp(40) - size).coerceAtLeast(1)
            val maxY = (dp(330) - size).coerceAtLeast(1)
            val x = Random.nextInt(maxX); val y = Random.nextInt(maxY)
            arena.addView(target, FrameLayout.LayoutParams(size, size).apply {
                leftMargin = x; topMargin = y
            })
        }
        root.addView(arena, LinearLayout.LayoutParams(-1, dp(330)))
        spawn()
        root.addView(button("Reset skor") { points = 0; score.text = "Skor: 0"; spawn() })
        root.addView(button("Kembali") { showHome() })
    }
    private fun showEspDemo() {
        val root = base("ESP Demo")
        panel(root, "Target simulasi", "Kotak dan jarak di bawah dibuat dari data dummy lokal. Tidak membaca layar, posisi, atau memori Free Fire.")
        val board = object : View(this) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mint; style = Paint.Style.STROKE; strokeWidth = dp(2).toFloat(); textSize = dp(13).toFloat() }
            override fun onDraw(c: Canvas) {
                super.onDraw(c)
                val targets = listOf(0.25f to 0.28f, 0.68f to 0.43f, 0.48f to 0.72f)
                targets.forEachIndexed { i, t ->
                    val x = width * t.first; val y = height * t.second
                    c.drawRect(x-dp(25), y-dp(38), x+dp(25), y+dp(38), p)
                    c.drawText("Dummy ${i+1}", x-dp(25), y-dp(45), p)
                    c.drawText("${8+i*7} m", x-dp(14), y+dp(55), p)
                }
            }
        }.apply { setBackgroundColor(Color.rgb(8, 11, 16)) }
        root.addView(board, LinearLayout.LayoutParams(-1, dp(350)))
        root.addView(button("Kembali") { showHome() })
    }
}
