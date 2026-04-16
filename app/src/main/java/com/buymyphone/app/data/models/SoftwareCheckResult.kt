package com.buymyphone.app.data.models

data class SoftwareCheckResult(
    val checks: List<SoftwareCheck> = emptyList(),
    val overallRisk: RiskLevel = RiskLevel.LOW,
    val riskSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SoftwareCheck(
    val id: String,
    val title: String,
    val description: String,
    val status: CheckStatus,
    val detail: String = "",
    val category: CheckCategory = CheckCategory.SECURITY
)

enum class CheckStatus(val label: String) {
    PASS("Pass"),
    WARNING("Warning"),
    FAIL("Fail"),
    INFO("Info"),
    UNKNOWN("Unknown")
}

enum class CheckCategory {
    SECURITY, BATTERY, DISPLAY, STORAGE, SENSOR, INTEGRITY, PERFORMANCE
}

enum class RiskLevel(val label: String, val colorHex: String) {
    LOW("Low Risk – Safe to Buy", "#4CAF50"),
    MEDIUM("Medium Risk – Inspect Carefully", "#FF9800"),
    HIGH("High Risk – Avoid or Negotiate", "#F44336")
}
