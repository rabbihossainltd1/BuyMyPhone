package com.buymyphone.app.data.models

data class HardwareTestItem(
    val id: String,
    val title: String,
    val description: String,
    val category: HardwareCategory,
    var status: HardwareTestStatus = HardwareTestStatus.PENDING,
    var detail: String = "",
    val isManual: Boolean = false,
    val requiresPermission: Boolean = false,
    val permissionNeeded: String = ""
)

enum class HardwareTestStatus {
    PENDING, RUNNING, PASS, FAIL, SKIP, MANUAL_PASS, MANUAL_FAIL
}

enum class HardwareCategory(val label: String) {
    DISPLAY("Display"),
    AUDIO("Audio"),
    SENSORS("Sensors"),
    CAMERA("Camera"),
    CONNECTIVITY("Connectivity"),
    INPUT("Input"),
    SYSTEM("System")
}
