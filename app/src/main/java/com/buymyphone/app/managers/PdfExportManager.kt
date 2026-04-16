package com.buymyphone.app.managers

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.buymyphone.app.data.models.*
import com.buymyphone.app.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class PdfExportManager(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH  = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN      = 48f
        private const val CONTENT_W   = PAGE_WIDTH - 2 * MARGIN
    }

    suspend fun exportAnalysisReport(result: AnalysisResult): String = withContext(Dispatchers.IO) {
        val doc = PdfDocument()
        var pageNum = 1

        // Page 1: Cover
        pageNum = addCoverPage(doc, result.deviceInfo, result.overallScore,
            result.performanceClass.label, pageNum)

        // Page 2: Device Specs
        pageNum = addDeviceSpecsPage(doc, result.deviceInfo, pageNum)

        // Page 3: Scores & Verdict
        pageNum = addScoresPage(doc, result, pageNum)

        // Page 4: Recommendations
        addRecommendationsPage(doc, result, pageNum)

        saveDocument(doc, "Analysis_${System.currentTimeMillis()}")
    }

    suspend fun exportSoftwareReport(result: SoftwareCheckResult, deviceInfo: DeviceInfo): String =
        withContext(Dispatchers.IO) {
            val doc = PdfDocument()
            var pageNum = addCoverPage(doc, deviceInfo, -1, "Software Analysis", 1)
            pageNum = addSoftwareChecksPage(doc, result, pageNum)
            saveDocument(doc, "SoftwareReport_${System.currentTimeMillis()}")
        }

    suspend fun exportBuySellReport(result: BuySellResult, deviceInfo: DeviceInfo): String =
        withContext(Dispatchers.IO) {
            val doc = PdfDocument()
            var pageNum = addCoverPage(doc, deviceInfo, result.overallScore, "Buy / Sell Report", 1)
            pageNum = addBuySellPage(doc, result, pageNum)
            saveDocument(doc, "BuySell_${System.currentTimeMillis()}")
        }

    // ── Page builders ────────────────────────────────────────────────────────

    private fun addCoverPage(
        doc: PdfDocument, device: DeviceInfo, score: Int,
        reportType: String, pageNum: Int
    ): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        val c = page.canvas

        // Background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, PAGE_HEIGHT.toFloat(),
                Color.parseColor("#0D1B2A"), Color.parseColor("#1B3A4B"),
                Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // App name
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("BuyMyPhone", PAGE_WIDTH / 2f, 120f, titlePaint)

        // Tagline
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#80FFFFFF")
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        c.drawText("Professional Device Analysis Report", PAGE_WIDTH / 2f, 148f, subPaint)

        // Divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#40FFFFFF")
            strokeWidth = 1f
        }
        c.drawLine(MARGIN, 165f, PAGE_WIDTH - MARGIN, 165f, divPaint)

        // Report Type
        val typePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64B5F6")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText(reportType, PAGE_WIDTH / 2f, 200f, typePaint)

        // Device name
        val devicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText(device.deviceName.ifBlank { "Unknown Device" }, PAGE_WIDTH / 2f, 240f, devicePaint)

        val modelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#80FFFFFF")
            textSize = 13f
            textAlign = Paint.Align.CENTER
        }
        c.drawText("Android ${device.androidVersion}  •  API ${device.apiLevel}", PAGE_WIDTH / 2f, 262f, modelPaint)

        // Score circle (only if score > 0)
        if (score > 0) {
            val cx = PAGE_WIDTH / 2f
            val cy = 400f
            val radius = 70f

            val ringBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#20FFFFFF")
                style = Paint.Style.STROKE
                strokeWidth = 12f
            }
            c.drawCircle(cx, cy, radius, ringBg)

            val ringColor = when {
                score >= 85 -> Color.parseColor("#4CAF50")
                score >= 70 -> Color.parseColor("#2196F3")
                score >= 50 -> Color.parseColor("#FF9800")
                else        -> Color.parseColor("#F44336")
            }
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ringColor
                style = Paint.Style.STROKE
                strokeWidth = 12f
                strokeCap = Paint.Cap.ROUND
            }
            val sweep = (score / 100f) * 360f
            c.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                -90f, sweep, false, ringPaint)

            val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 38f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            c.drawText("$score", cx, cy + 14f, scorePaint)

            val scoreLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#80FFFFFF")
                textSize = 11f
                textAlign = Paint.Align.CENTER
            }
            c.drawText("/ 100", cx, cy + 30f, scoreLabelPaint)
        }

        // Timestamp
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#60FFFFFF")
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }
        val dateStr = SimpleDateFormat("MMMM dd, yyyy  hh:mm a", Locale.getDefault()).format(Date())
        c.drawText("Generated: $dateStr", PAGE_WIDTH / 2f, PAGE_HEIGHT - 48f, datePaint)
        c.drawText("Page $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 30f, datePaint)

        doc.finishPage(page)
        return pageNum + 1
    }

    private fun addDeviceSpecsPage(doc: PdfDocument, device: DeviceInfo, pageNum: Int): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        val c = page.canvas
        drawPageBackground(c)
        drawPageHeader(c, "Device Specifications", pageNum)

        var y = 100f

        val sections = mapOf(
            "Identity" to listOf(
                "Manufacturer" to device.manufacturer,
                "Model"        to device.model,
                "Brand"        to device.brand,
                "Android Ver." to device.androidVersion,
                "API Level"    to device.apiLevel.toString(),
                "Security Patch" to device.securityPatch,
                "Build Number" to device.buildNumber
            ),
            "Processor" to listOf(
                "SoC"          to device.socName,
                "CPU"          to device.cpuModel,
                "Cores"        to device.cpuCores.toString(),
                "Architecture" to device.cpuArchitecture,
                "CPU ABI"      to device.cpuAbi,
                "GPU"          to device.gpuModel
            ),
            "Memory & Storage" to listOf(
                "Total RAM"    to "${device.totalRamMB / 1024} GB (${device.totalRamMB} MB)",
                "Free RAM"     to "${device.availableRamMB} MB",
                "Storage"      to "${String.format("%.0f", device.totalStorageGB)} GB total",
                "Free Storage" to "${String.format("%.1f", device.availableStorageGB)} GB",
                "Storage Type" to device.storageType
            ),
            "Display" to listOf(
                "Resolution"   to "${device.screenWidthPx}×${device.screenHeightPx}",
                "Density"      to "${device.screenDensityDpi} dpi",
                "Screen Size"  to "${device.screenSizeInch} inches",
                "Refresh Rate" to "${device.screenRefreshRate} Hz",
                "HDR Support"  to if (device.hdrSupport) "Yes" else "No"
            )
        )

        for ((section, rows) in sections) {
            y = drawSection(c, section, rows, y)
            y += 12f
            if (y > PAGE_HEIGHT - 80) break
        }

        drawPageFooter(c, pageNum)
        doc.finishPage(page)
        return pageNum + 1
    }

    private fun addScoresPage(doc: PdfDocument, result: AnalysisResult, pageNum: Int): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        val c = page.canvas
        drawPageBackground(c)
        drawPageHeader(c, "Performance Scores", pageNum)

        var y = 110f

        val scores = mapOf(
            "Overall Score"     to result.overallScore,
            "Performance"       to result.performanceScore,
            "Display"           to result.displayScore,
            "Camera"            to result.cameraScore,
            "Battery"           to result.batteryScore,
            "Build Quality"     to result.buildQualityScore
        )

        for ((label, value) in scores) {
            drawScoreBar(c, label, value, y)
            y += 44f
        }

        y += 20f
        drawSectionTitle(c, "Verdict", y); y += 28f

        val rows = listOf(
            "Performance Class" to result.performanceClass.label,
            "Best Use Case"     to result.bestUsage.label,
            "Usage Verdict"     to "${result.bestUsage.emoji} ${result.bestUsage.label}"
        )
        y = drawSection(c, "", rows, y)

        drawPageFooter(c, pageNum)
        doc.finishPage(page)
        return pageNum + 1
    }

    private fun addRecommendationsPage(doc: PdfDocument, result: AnalysisResult, pageNum: Int): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        val c = page.canvas
        drawPageBackground(c)
        drawPageHeader(c, "Analysis Summary", pageNum)

        var y = 110f

        if (result.highlights.isNotEmpty()) {
            drawSectionTitle(c, "✅  Highlights", y); y += 28f
            for (item in result.highlights) { y = drawBullet(c, item, y) }
            y += 10f
        }

        if (result.weaknesses.isNotEmpty()) {
            drawSectionTitle(c, "⚠️  Weaknesses", y); y += 28f
            for (item in result.weaknesses) { y = drawBullet(c, item, y) }
            y += 10f
        }

        if (result.recommendations.isNotEmpty()) {
            drawSectionTitle(c, "💡  Recommendations", y); y += 28f
            for (item in result.recommendations) { y = drawBullet(c, item, y) }
        }

        drawPageFooter(c, pageNum)
        doc.finishPage(page)
        return pageNum + 1
    }

    private fun addSoftwareChecksPage(doc: PdfDocument, result: SoftwareCheckResult, pageNum: Int): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        val c = page.canvas
        drawPageBackground(c)
        drawPageHeader(c, "Software Analysis Results", pageNum)

        var y = 100f

        // Risk banner
        val riskColor = Color.parseColor(result.overallRisk.colorHex)
        val riskBg = Paint().apply { color = riskColor; alpha = 30 }
        c.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 40f, 8f, 8f, riskBg)
        val riskTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = riskColor
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        c.drawText(result.overallRisk.label, MARGIN + 12f, y + 26f, riskTextPaint)
        y += 54f

        for (check in result.checks) {
            if (y > PAGE_HEIGHT - 80) break
            val statusColor = when (check.status) {
                CheckStatus.PASS    -> Color.parseColor("#4CAF50")
                CheckStatus.WARNING -> Color.parseColor("#FF9800")
                CheckStatus.FAIL    -> Color.parseColor("#F44336")
                else                -> Color.parseColor("#9E9E9E")
            }
            val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = statusColor }
            c.drawCircle(MARGIN + 6f, y + 8f, 5f, dot)

            val titleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0E0E0")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            c.drawText(check.title, MARGIN + 18f, y + 12f, titleP)

            val detailP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#9E9E9E")
                textSize = 11f
            }
            c.drawText(check.detail.take(80), MARGIN + 18f, y + 26f, detailP)
            y += 40f
        }

        drawPageFooter(c, pageNum)
        doc.finishPage(page)
        return pageNum + 1
    }

    private fun addBuySellPage(doc: PdfDocument, result: BuySellResult, pageNum: Int): Int {
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        val c = page.canvas
        drawPageBackground(c)
        drawPageHeader(c, "Buy / Sell Recommendation", pageNum)

        var y = 100f

        val verdictColor = Color.parseColor(result.verdict.colorHex)
        val verdictBg = Paint().apply { color = verdictColor; alpha = 40 }
        c.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 55f, 12f, 12f, verdictBg)
        val verdictP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = verdictColor
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("${result.verdict.emoji}  ${result.verdict.label}", PAGE_WIDTH / 2f, y + 35f, verdictP)
        y += 75f

        val rows = listOf(
            "Overall Score"   to "${result.overallScore}/100",
            "Software Risk"   to result.softwareRisk,
            "Hardware Score"  to "${result.hardwareScore}/100",
            "Fair Price"      to result.fairPriceRange,
            "Confidence"      to "${result.confidenceLevel}%"
        )
        y = drawSection(c, "Summary", rows, y)
        y += 16f

        if (result.whyBuy.isNotEmpty()) {
            drawSectionTitle(c, "✅  Why Buy", y); y += 26f
            for (item in result.whyBuy) { y = drawBullet(c, item, y) }
            y += 8f
        }

        if (result.whyAvoid.isNotEmpty()) {
            drawSectionTitle(c, "⚠️  Why Avoid", y); y += 26f
            for (item in result.whyAvoid) { y = drawBullet(c, item, y) }
            y += 8f
        }

        drawSectionTitle(c, "Resale Verdict", y); y += 26f
        y = drawWrappedText(c, result.resaleVerdict, y)
        y += 12f

        drawSectionTitle(c, "Buyer Warning", y); y += 26f
        y = drawWrappedText(c, result.buyerWarning, y)
        y += 12f

        drawSectionTitle(c, "Seller Recommendation", y); y += 26f
        drawWrappedText(c, result.sellerRecommendation, y)

        drawPageFooter(c, pageNum)
        doc.finishPage(page)
        return pageNum + 1
    }

    // ── Drawing helpers ──────────────────────────────────────────────────────

    private fun drawPageBackground(c: Canvas) {
        val bg = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, PAGE_HEIGHT.toFloat(),
                Color.parseColor("#0D1B2A"), Color.parseColor("#162032"),
                Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bg)
    }

    private fun drawPageHeader(c: Canvas, title: String, pageNum: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64B5F6")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        c.drawText(title, MARGIN, 60f, paint)
        val line = Paint().apply { color = Color.parseColor("#30FFFFFF"); strokeWidth = 1f }
        c.drawLine(MARGIN, 70f, PAGE_WIDTH - MARGIN, 70f, line)
    }

    private fun drawPageFooter(c: Canvas, pageNum: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#50FFFFFF")
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        c.drawLine(MARGIN, PAGE_HEIGHT - 40f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 40f,
            Paint().apply { color = Color.parseColor("#20FFFFFF") })
        c.drawText("BuyMyPhone  •  Page $pageNum", PAGE_WIDTH / 2f, PAGE_HEIGHT - 24f, paint)
    }

    private fun drawSectionTitle(c: Canvas, title: String, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64B5F6")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        c.drawText(title, MARGIN, y, paint)
    }

    private fun drawSection(c: Canvas, title: String, rows: List<Pair<String, String>>, startY: Float): Float {
        var y = startY
        if (title.isNotBlank()) { drawSectionTitle(c, title, y); y += 22f }

        val labelP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9E9E9E"); textSize = 11f
        }
        val valueP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0"); textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for ((label, value) in rows) {
            c.drawText(label, MARGIN + 4f, y, labelP)
            c.drawText(value.take(50), MARGIN + CONTENT_W / 2f, y, valueP)
            y += 18f
        }
        return y
    }

    private fun drawScoreBar(c: Canvas, label: String, score: Int, y: Float) {
        val labelP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0"); textSize = 12f
        }
        c.drawText(label, MARGIN, y + 14f, labelP)

        val barBg = Paint().apply { color = Color.parseColor("#20FFFFFF") }
        val barLeft  = MARGIN + 160f
        val barRight = PAGE_WIDTH - MARGIN - 50f
        val barW = barRight - barLeft
        c.drawRoundRect(barLeft, y + 4f, barRight, y + 20f, 6f, 6f, barBg)

        val color = when {
            score >= 85 -> Color.parseColor("#4CAF50")
            score >= 70 -> Color.parseColor("#2196F3")
            score >= 50 -> Color.parseColor("#FF9800")
            else        -> Color.parseColor("#F44336")
        }
        val barFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(barLeft, 0f, barLeft + barW * score / 100f, 0f,
                color, Color.parseColor(
                    if (score >= 85) "#81C784" else if (score >= 70) "#64B5F6"
                    else if (score >= 50) "#FFB74D" else "#E57373"
                ), Shader.TileMode.CLAMP)
        }
        c.drawRoundRect(barLeft, y + 4f, barLeft + barW * score / 100f, y + 20f, 6f, 6f, barFill)

        val s
