package com.buymyphone.app.utils

import com.buymyphone.app.data.models.*

object ScoringEngine {

    fun calculateAnalysisResult(deviceInfo: DeviceInfo): AnalysisResult {
        val performanceScore = calculatePerformanceScore(deviceInfo)
        val displayScore    = calculateDisplayScore(deviceInfo)
        val cameraScore     = calculateCameraScore(deviceInfo)
        val batteryScore    = calculateBatteryScore(deviceInfo)
        val buildScore      = calculateBuildScore(deviceInfo)

        val overallScore = (
            performanceScore * 0.35 +
            displayScore    * 0.20 +
            cameraScore     * 0.20 +
            batteryScore    * 0.15 +
            buildScore      * 0.10
        ).toInt().coerceIn(0, 100)

        val performanceClass = when {
            overallScore >= 85 -> PerformanceClass.FLAGSHIP
            overallScore >= 70 -> PerformanceClass.UPPER_MIDRANGE
            overallScore >= 50 -> PerformanceClass.MIDRANGE
            else               -> PerformanceClass.ENTRY_LEVEL
        }

        val bestUsage = determineBestUsage(deviceInfo, performanceScore, cameraScore, displayScore)
        val highlights = buildHighlights(deviceInfo, overallScore)
        val weaknesses = buildWeaknesses(deviceInfo, overallScore)
        val recommendations = buildRecommendations(deviceInfo, performanceClass)

        return AnalysisResult(
            deviceInfo        = deviceInfo,
            overallScore      = overallScore,
            performanceScore  = performanceScore,
            displayScore      = displayScore,
            cameraScore       = cameraScore,
            batteryScore      = batteryScore,
            buildQualityScore = buildScore,
            performanceClass  = performanceClass,
            bestUsage         = bestUsage,
            highlights        = highlights,
            weaknesses        = weaknesses,
            recommendations   = recommendations
        )
    }

    private fun calculatePerformanceScore(info: DeviceInfo): Int {
        val socInfo = SoCDatabase.findSoCByKeyword(info.socName)
            ?: SoCDatabase.findSoCByKeyword(info.hardware)
            ?: SoCDatabase.findSoCByKeyword(info.cpuModel)

        var score = socInfo?.score ?: 50

        // RAM bonus
        score += when {
            info.totalRamMB >= 12 * 1024 -> 5
            info.totalRamMB >= 8 * 1024  -> 3
            info.totalRamMB >= 6 * 1024  -> 1
            info.totalRamMB < 4 * 1024   -> -5
            else -> 0
        }

        // Storage type bonus
        score += when (info.storageType.lowercase()) {
            "ufs 3.1", "ufs3.1" -> 3
            "ufs 3.0", "ufs3.0" -> 2
            "ufs 2.1", "ufs2.1" -> 1
            else -> 0
        }

        return score.coerceIn(0, 100)
    }

    private fun calculateDisplayScore(info: DeviceInfo): Int {
        var score = 50

        val totalPx = info.screenWidthPx.toLong() * info.screenHeightPx
        score += when {
            totalPx >= 3_000_000 -> 15  // QHD+
            totalPx >= 2_000_000 -> 10  // FHD+
            totalPx >= 1_000_000 -> 5   // HD+
            else                 -> 0
        }

        score += when {
            info.screenRefreshRate >= 144 -> 15
            info.screenRefreshRate >= 120 -> 10
            info.screenRefreshRate >= 90  -> 5
            else                          -> 0
        }

        score += when {
            info.screenSizeInch >= 6.7 -> 5
            info.screenSizeInch >= 6.1 -> 3
            info.screenSizeInch >= 5.5 -> 1
            else                        -> 0
        }

        if (info.hdrSupport) score += 10
        if (info.screenDensityDpi >= 400) score += 5

        return score.coerceIn(0, 100)
    }

    private fun calculateCameraScore(info: DeviceInfo): Int {
        var score = 40

        val mainCamera = info.rearCamerasMegapixels.maxOrNull() ?: 0.0
        score += when {
            mainCamera >= 200 -> 25
            mainCamera >= 108 -> 20
            mainCamera >= 64  -> 15
            mainCamera >= 48  -> 10
            mainCamera >= 16  -> 5
            else              -> 0
        }

        score += (info.rearCamerasMegapixels.size.coerceAtMost(4) - 1) * 5

        val frontCamera = info.frontCamerasMegapixels.maxOrNull() ?: 0.0
        score += when {
            frontCamera >= 32 -> 10
            frontCamera >= 16 -> 7
            frontCamera >= 8  -> 4
            else              -> 0
        }

        if (info.hasOIS) score += 10
        if (info.hasNightMode) score += 5

        return score.coerceIn(0, 100)
    }

    private fun calculateBatteryScore(info: DeviceInfo): Int {
        var score = 50

        // Capacity scoring (NOT using level %)
        score += when {
            info.batteryCapacityMah >= 5000 -> 20
            info.batteryCapacityMah >= 4500 -> 15
            info.batteryCapacityMah >= 4000 -> 10
            info.batteryCapacityMah >= 3500 -> 5
            info.batteryCapacityMah > 0     -> 0
            else                            -> -10
        }

        // Temperature penalty
        score -= when {
            info.batteryTemperatureCelsius > 45 -> 25
            info.batteryTemperatureCelsius > 40 -> 15
            info.batteryTemperatureCelsius > 37 -> 5
            else                                -> 0
        }

        return score.coerceIn(0, 100)
    }

    private fun calculateBuildScore(info: DeviceInfo): Int {
        var score = 60

        if (info.hasNfc) score += 10
        if (info.hasFingerprint) score += 5
        if (info.has5G) score += 10
        if (info.hasBarometer) score += 3
        if (info.hasGyroscope) score += 5
        if (info.hasOIS) score += 7

        val sensorCount = listOf(
            info.hasAccelerometer, info.hasGyroscope, info.hasCompass,
            info.hasProximity, info.hasLightSensor, info.hasBarometer
        ).count { it }

        score += sensorCount * 2

        return score.coerceIn(0, 100)
    }

    private fun determineBestUsage(
        info: DeviceInfo,
        perfScore: Int,
        camScore: Int,
        dispScore: Int
    ): BestUsage {
        val scores = mapOf(
            BestUsage.GAMING       to (perfScore * 0.6 + dispScore * 0.4),
            BestUsage.CAMERA       to (camScore * 0.7 + perfScore * 0.3),
            BestUsage.DAILY_USE    to (perfScore * 0.4 + dispScore * 0.3 + camScore * 0.3),
            BestUsage.MULTITASKING to ((info.totalRamMB / 1024.0).coerceAtMost(12.0) * 8 + perfScore * 0.5)
        )
        return scores.maxByOrNull { it.value }?.key ?: BestUsage.DAILY_USE
    }

    private fun buildHighlights(info: DeviceInfo, score: Int): List<String> {
        val list = mutableListOf<String>()
        val socInfo = SoCDatabase.findSoCByKeyword(info.socName)

        if (socInfo != null && socInfo.score >= 85) list.add("Premium ${socInfo.name} processor")
        if (info.screenRefreshRate >= 120) list.add("Smooth ${info.screenRefreshRate}Hz display")
        if (info.rearCamerasMegapixels.maxOrNull() ?: 0.0 >= 50)
            list.add("${info.rearCamerasMegapixels.maxOrNull()?.toInt()}MP main camera")
        if (info.batteryCapacityMah >= 4500) list.add("Large ${info.batteryCapacityMah}mAh battery")
        if (info.totalRamMB >= 8192) list.add("${info.totalRamMB / 1024}GB RAM for smooth multitasking")
        if (info.has5G) list.add("5G connectivity ready")
        if (info.hdrSupport) list.add("HDR display support")
        if (info.hasOIS) list.add("Optical image stabilization")

        return list.take(5)
    }

    private fun buildWeaknesses(info: DeviceInfo, score: Int): List<String> {
        val list = mutableListOf<String>()

        if (info.totalRamMB < 4096) list.add("Low RAM (${info.totalRamMB / 1024}GB)")
        if (info.batteryCapacityMah in 1..3499) list.add("Small battery (${info.batteryCapacityMah}mAh)")
        if (info.screenRefreshRate <= 60) list.add("Basic 60Hz display")
        if (!info.has5G) list.add("No 5G support")
        if (!info.hasNfc) list.add("No NFC for contactless payment")
        if (!info.hasOIS) list.add("No optical image stabilization")
        if (info.batteryTemperatureCelsius > 40) list.add("Battery running hot")

        return list.take(4)
    }

    private fun buildRecommendations(info: DeviceInfo, cls: PerformanceClass): List<String> {
        return when (cls) {
            PerformanceClass.FLAGSHIP -> listOf(
                "Excellent choice for power users & content creators",
                "Ideal for gaming and heavy multitasking",
                "Future-proof for at least 3–4 years"
            )
            PerformanceClass.UPPER_MIDRANGE -> listOf(
                "Great daily driver with premium features",
                "Handles gaming and productivity well",
                "Good value proposition"
            )
            PerformanceClass.MIDRANGE -> listOf(
                "Suitable for everyday tasks",
                "Good for social media and light gaming",
                "May struggle with demanding applications"
            )
            PerformanceClass.ENTRY_LEVEL -> listOf(
                "Best for basic communication tasks",
                "Not recommended for gaming or heavy use",
                "Negotiate aggressively on price"
            )
        }
    }

    fun calculateBuySellResult(
        softwareResult: SoftwareCheckResult,
        hardwareScore: Int,
        deviceInfo: DeviceInfo
    ): com.buymyphone.app.data.models.BuySellResult {
        val softwarePenalty = when (softwareResult.overallRisk) {
            RiskLevel.LOW    -> 0
            RiskLevel.MEDIUM -> 15
            RiskLevel.HIGH   -> 35
        }

        val overallScore = (hardwareScore - softwarePenalty).coerceIn(0, 100)

        val verdict = when {
            overallScore >= 80 && softwareResult.overallRisk == RiskLevel.LOW ->
                BuySellVerdict.WORTH_BUYING
            overallScore >= 65 ->
                BuySellVerdict.GOOD_DEAL
            overallScore >= 45 ->
                BuySellVerdict.OVERPRICED
            else ->
                BuySellVerdict.AVOID
        }

        val whyBuy = mutableListOf<String>()
        val whyAvoid = mutableListOf<String>()

        if (softwareResult.overallRisk == RiskLevel.LOW) whyBuy.add("Software integrity looks clean")
        if (hardwareScore >= 70) whyBuy.add("Hardware passed most diagnostic tests")
        if (deviceInfo.batteryTemperatureCelsius < 37) whyBuy.add("Battery temperature is normal")
        if (deviceInfo.has5G) whyBuy.add("Future-proof 5G connectivity")

        softwareResult.checks.filter { it.status == CheckStatus.FAIL }.forEach {
            whyAvoid.add(it.title + ": " + it.detail.take(50))
        }
        if (deviceInfo.batteryTemperatureCelsius > 40) whyAvoid.add("High battery temperature detected")

        return com.buymyphone.app.data.models.BuySellResult(
            verdict = verdict,
            overallScore = overallScore,
            softwareRisk = softwareResult.overallRisk.label,
            hardwareScore = hardwareScore,
            whyBuy = whyBuy.take(5),
            whyAvoid = whyAvoid.take(5),
            resaleVerdict = buildResaleVerdict(verdict, deviceInfo),
            buyerWarning = buildBuyerWarning(softwareResult, deviceInfo),
            sellerRecommendation = buildSellerRecommendation(verdict, overallScore),
            fairPriceRange = estimateFairPrice(overallScore, deviceInfo),
            confidenceLevel = calculateConfidence(softwareResult, hardwareScore)
        )
    }

    private fun buildResaleVerdict(verdict: BuySellVerdict, info: DeviceInfo): String {
        return when (verdict) {
            BuySellVerdict.WORTH_BUYING -> "Strong resale value expected. Device condition aligns with market pricing."
            BuySellVerdict.GOOD_DEAL -> "Moderate resale value. Minor issues may affect future selling price."
            BuySellVerdict.OVERPRICED -> "Resale value may drop quickly. Negotiate seller down before buying."
            BuySellVerdict.AVOID -> "Poor resale prospects. Significant issues reduce device value substantially."
        }
    }

    private fun buildBuyerWarning(result: SoftwareCheckResult, info: DeviceInfo): String {
        val warnings = mutableListOf<String>()
        if (result.checks.any { it.id == "root" && it.status == CheckStatus.FAIL })
            warnings.add("Device may be rooted")
        if (result.checks.any { it.id == "bootloader" && it.status == CheckStatus.WARNING })
            warnings.add("Bootloader unlock suspected")
        if (info.batteryTemperatureCelsius > 40) warnings.add("Overheating battery")
        if (result.overallRisk == RiskLevel.HIGH) warnings.add("Multiple critical checks failed")
        return if (warnings.isEmpty()) "No major warnings detected." else warnings.joinToString(" • ")
    }

    private fun buildSellerRecommendation(verdict: BuySellVerdict, score: Int): String {
        return when (verdict) {
            BuySellVerdict.WORTH_BUYING, BuySellVerdict.GOOD_DEAL ->
                "Price competitively for a quick sale. Device condition supports asking price."
            BuySellVerdict.OVERPRICED ->
                "Consider reducing asking price by 15–20% to attract buyers."
            BuySellVerdict.AVOID ->
                "Disclose all issues upfront. Price significantly below market to sell."
        }
    }

    private fun estimateFairPrice(score: Int, info: DeviceInfo): String {
        return when {
            score >= 85 -> "Market price or slight discount (–5%)"
            score >= 70 -> "Moderate discount (–10% to –20%)"
            score >= 50 -> "Significant discount (–20% to –35%)"
            else        -> "Heavy discount required (–35% or more)"
        }
    }

    private fun calculateConfidence(result: SoftwareCheckResult, hwScore: Int): Int {
        val total = result.checks.size
        val checked = result.checks.count { it.status != CheckStatus.UNKNOWN }
        return ((checked.toFloat() / total.coerceAtLeast(1)) * 100).toInt()
    }
}
