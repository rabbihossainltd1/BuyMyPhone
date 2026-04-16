package com.buymyphone.app.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.buymyphone.app.data.database.entities.ReportEntity

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): LiveData<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    suspend fun getAllReportsSuspend(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE id = :reportId LIMIT 1")
    suspend fun getReportById(reportId: Long): ReportEntity?

    @Query("SELECT * FROM reports WHERE reportType = :type ORDER BY timestamp DESC")
    fun getReportsByType(type: String): LiveData<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteReportById(reportId: Long)

    @Query("DELETE FROM reports")
    suspend fun deleteAllReports()

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun getReportCount(): Int
}
