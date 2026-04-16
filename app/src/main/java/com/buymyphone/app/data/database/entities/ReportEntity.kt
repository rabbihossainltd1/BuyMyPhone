package com.buymyphone.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceName: String,
    val deviceModel: String,
    val reportType: String,         // "STANDARD", "SOFTWARE", "HARDWARE", "BUYSELL"
    val score: Int,
    val performanceClass: String,
    val bestUsage: String,
    val timestamp: Long,
    val analysisJson: String,       // Full JSON of analysis data
    val pdfPath: String = "",
    val thumbnailBase64: String = ""
)
