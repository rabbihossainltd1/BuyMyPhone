package com.buymyphone.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.buymyphone.app.data.database.entities.ReportEntity
import com.buymyphone.app.databinding.ItemHistoryReportBinding
import com.buymyphone.app.utils.toFormattedDate
import com.buymyphone.app.utils.scoreToColor

class HistoryAdapter(
    private val onItemClick: (ReportEntity) -> Unit,
    private val onDeleteClick: (ReportEntity) -> Unit,
    private val onExportClick: (ReportEntity) -> Unit
) : ListAdapter<ReportEntity, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: ReportEntity) {
            with(binding) {
                tvDeviceName.text    = report.deviceName.ifBlank { "Unknown Device" }
                tvDeviceModel.text   = report.deviceModel
                tvReportType.text    = report.reportType
                tvTimestamp.text     = report.timestamp.toFormattedDate()
                tvScore.text         = "${report.score}"
                tvPerfClass.text     = report.performanceClass

                tvScore.setTextColor(report.score.scoreToColor())

                chipReportType.text = when (report.reportType) {
                    "STANDARD" -> "📊 Standard"
                    "SOFTWARE" -> "🔍 Software"
                    "HARDWARE" -> "🔧 Hardware"
                    "BUYSELL"  -> "💰 Buy/Sell"
                    else       -> report.reportType
                }

                root.setOnClickListener  { onItemClick(report) }
                btnDelete.setOnClickListener { onDeleteClick(report) }
                btnExport.setOnClickListener { onExportClick(report) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportEntity>() {
        override fun areItemsTheSame(a: ReportEntity, b: ReportEntity) = a.id == b.id
        override fun areContentsTheSame(a: ReportEntity, b: ReportEntity) = a == b
    }
}
