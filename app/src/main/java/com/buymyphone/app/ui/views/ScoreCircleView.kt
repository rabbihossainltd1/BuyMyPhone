package com.buymyphone.app.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.buymyphone.app.utils.Constants
import kotlin.math.min

class ScoreCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var targetScore    = 0
    private var currentScore   = 0f
    private var scoreLabel     = "Score"
    private var performanceLabel = ""

    private val backgroundRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style      = Paint.Style.STROKE
        strokeWidth = 18f
        color      = Color.parseColor("#1A2E44")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style      = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap  = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style      = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap  = Paint.Cap.ROUND
    }

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.parseColor("#90B4D1")
        textAlign = Paint.Align.CENTER
    }

    private val perfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val ovalRect = RectF()
    private var animator: ValueAnimator? = null

    fun setScore(score: Int, label: String = "Score", perfLabel: String = "") {
        targetScore = score.coerceIn(0, 100)
        scoreLabel  = label
        performanceLabel = perfLabel
        animateToScore()
    }

    private fun animateToScore() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, targetScore.toFloat()).apply {
            duration     = Constants.ANIMATION_DURATION_SCORE
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener {
                currentScore = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val cx   = width  / 2f
        val cy   = height / 2f
        val ringRadius = size / 2f - 30f
        val pad  = (size / 2f) - ringRadius

        ovalRect.set(cx - ringRadius, cy - ringRadius, cx + ringRadius, cy + ringRadius)

        // Background track
        canvas.drawOval(ovalRect, backgroundRingPaint)

        // Score-based colors
        val score = currentScore.toInt()
        val (primaryColor, secondaryColor) = when {
            score >= 85 -> Pair(Color.parseColor("#4CAF50"), Color.parseColor("#81C784"))
            score >= 70 -> Pair(Color.parseColor("#2196F3"), Color.parseColor("#64B5F6"))
            score >= 50 -> Pair(Color.parseColor("#FF9800"), Color.parseColor("#FFB74D"))
            else        -> Pair(Color.parseColor("#F44336"), Color.parseColor("#E57373"))
        }

        // Glow effect (shadow ring)
        glowPaint.color = primaryColor
        glowPaint.alpha = 30
        canvas.drawArc(ovalRect, -90f, (currentScore / 100f) * 360f, false, glowPaint)

        // Progress arc with gradient
        val sweep = (currentScore / 100f) * 360f
        val shader = SweepShader(cx, cy, intArrayOf(secondaryColor, primaryColor, primaryColor),
            floatArrayOf(0f, sweep / 360f, 1f))
        progressPaint.shader = shader
        progressPaint.alpha  = 255
        canvas.save()
        canvas.rotate(-90f, cx, cy)
        canvas.drawArc(ovalRect, 0f, sweep, false, progressPaint)
        canvas.restore()

        // Score number
        scorePaint.textSize = ringRadius * 0.52f
        canvas.drawText("${score}", cx, cy + ringRadius * 0.18f, scorePaint)

        // Score label
        labelPaint.textSize = ringRadius * 0.15f
        canvas.drawText(scoreLabel, cx, cy + ringRadius * 0.38f, labelPaint)

        // Performance badge below
        if (performanceLabel.isNotBlank()) {
            val badgeColor = when {
                targetScore >= 85 -> Color.parseColor("#4CAF50")
                targetScore >= 70 -> Color.parseColor("#2196F3")
                targetScore >= 50 -> Color.parseColor("#FF9800")
                else              -> Color.parseColor("#F44336")
            }
            perfPaint.color    = badgeColor
            perfPaint.textSize = ringRadius * 0.14f
            canvas.drawText(performanceLabel, cx, cy + ringRadius * 0.60f, perfPaint)
        }
    }
}

// Minimal SweepShader wrapper
private fun SweepShader(cx: Float, cy: Float, colors: IntArray, positions: FloatArray?): Shader {
    return SweepGradient(cx, cy, colors, positions)
}
