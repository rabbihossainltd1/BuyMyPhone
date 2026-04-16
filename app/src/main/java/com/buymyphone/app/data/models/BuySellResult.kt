package com.buymyphone.app.data.models

data class BuySellResult(
    val verdict: BuySellVerdict,
    val overallScore: Int,
    val softwareRisk: String,
    val hardwareScore: Int,
    val whyBuy: List<String>,
    val whyAvoid: List<String>,
    val resaleVerdict: String,
    val buyerWarning: String,
    val sellerRecommendation: String,
    val fairPriceRange: String,
    val confidenceLevel: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class BuySellVerdict(
    val label: String,
    val emoji: String,
    val colorHex: String,
    val bgColorHex: String
) {
    WORTH_BUYING("Worth Buying", "✅", "#FFFFFF", "#4CAF50"),
    GOOD_DEAL("Good Deal", "👍", "#FFFFFF", "#2196F3"),
    OVERPRICED("Overpriced", "⚠️", "#000000", "#FF9800"),
    AVOID("Avoid", "🚫", "#FFFFFF", "#F44336")
}
