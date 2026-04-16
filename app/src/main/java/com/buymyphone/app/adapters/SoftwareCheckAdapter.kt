package com.buymyphone.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.buymyphone.app.data.models.CheckStatus
import com.buymyphone.app.data.models.SoftwareCheck
import com.buymyphone.app.databinding.ItemSoftwareCheckBinding

class SoftwareCheckAdapter :
    ListAdapter<SoftwareCheck, SoftwareCheckAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSoftwareCheckBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSoftwareCheckBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(check: SoftwareCheck) {
            with(binding) {
                tvTitle.text       = check.title
                tvDetail.text      = check.detail
                tvCategory.text    = check.category.name

                val (iconText, color) = when (check.status) {
                    CheckStatus.PASS    -> Pair("✅", "#4CAF50")
                    CheckStatus.WARNING -> Pair("⚠️", "#FF9800")
                    CheckStatus.FAIL    -> Pair("❌", "#F44336")
                    CheckStatus.INFO    -> Pair("ℹ️", "#2196F3")
                    CheckStatus.UNKNOWN -> Pair("❓", "#9E9E9E")
                }

                tvStatusIcon.text = iconText
                tvStatusLabel.text = check.status.label
                tvStatusLabel.setTextColor(Color.parseColor(color))
                viewStatusIndicator.setBackgroundColor(Color.parseColor(color))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SoftwareCheck>() {
        override fun areItemsTheSame(a: SoftwareCheck, b: SoftwareCheck) = a.id == b.id
        override fun areContentsTheSame(a: SoftwareCheck, b: SoftwareCheck) = a == b
    }
    }
