package com.buymyphone.app.data.models

data class AnalysisResult(
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val overallScore: Int = 0,
    val performanceScore: Int = 0,
    val displayScore: Int = 0,
    val cameraScore: Int = 0,
    val batteryScore: Int = 0,
    val buildQualityScore: Int = 0,
    val performanceClass: PerformanceClass = PerformanceClass.MIDRANGE,
    val bestUsage: BestUsage = BestUsage.DAILY_USE,
    val timestamp: Long = System.currentTimeMillis(),
    val recommendations: List<String> = emptyList(),
    val highlights: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList()
)

enum class PerformanceClass(val label: String, val colorHex: String) {
    FLAGSHIP("Flagship", "#4CAF50"),
    UPPER_MIDRANGE("Upper Midrange", "#2196F3"),
    MIDRANGE("Midrange", "#FF9800"),
    ENTRY_LEVEL("Entry Level", "#F44336")
}

enum class BestUsage(val label: String, val emoji: String) {
    GAMING("Gaming", "🎮"),
    CAMERA("Camera", "📷"),
    DAILY_USE("Daily Use", "📱"),
    MULTITASKING("Multitasking", "⚡")
}
