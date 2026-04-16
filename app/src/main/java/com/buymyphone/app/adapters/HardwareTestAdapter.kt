package com.buymyphone.app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.buymyphone.app.data.models.HardwareTestItem
import com.buymyphone.app.data.models.HardwareTestStatus
import com.buymyphone.app.databinding.ItemHardwareTestBinding

class HardwareTestAdapter(
    private val onTestAction: (HardwareTestItem) -> Unit
) : ListAdapter<HardwareTestItem, HardwareTestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHardwareTestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateItem(item: HardwareTestItem) {
        val list = currentList.toMutableList()
        val idx = list.indexOfFirst { it.id == item.id }
        if (idx >= 0) { list[idx] = item; submitList(list) }
    }

    inner class ViewHolder(private val binding: ItemHardwareTestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HardwareTestItem) {
            with(binding) {
                tvTestTitle.text    = item.title
                tvTestDesc.text     = item.description
                tvTestCategory.text = item.category.label
                tvTestDetail.text   = item.detail

                val (statusText, color) = when (item.status) {
                    HardwareTestStatus.PENDING      -> Pair("⏳ Pending",  "#9E9E9E")
                    HardwareTestStatus.RUNNING      -> Pair("🔄 Testing…", "#2196F3")
                    HardwareTestStatus.PASS         -> Pair("✅ Pass",     "#4CAF50")
                    HardwareTestStatus.FAIL         -> Pair("❌ Fail",     "#F44336")
                    HardwareTestStatus.SKIP         -> Pair("⏭ Skipped",  "#9E9E9E")
                    HardwareTestStatus.MANUAL_PASS  -> Pair("✅ Pass",     "#4CAF50")
                    HardwareTestStatus.MANUAL_FAIL  -> Pair("❌ Fail",     "#F44336")
                }

                tvStatus.text = statusText
                tvStatus.setTextColor(Color.parseColor(color))
                viewStatusBar.setBackgroundColor(Color.parseColor(color))

                btnAction.text = when (item.status) {
                    HardwareTestStatus.PENDING,
                    HardwareTestStatus.SKIP -> if (item.isManual) "Test" else "Run"
                    HardwareTestStatus.RUNNING -> "Running…"
                    else -> "Re-test"
                }

                btnAction.isEnabled = item.status != HardwareTestStatus.RUNNING
                btnAction.setOnClickListener { onTestAction(item) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HardwareTestItem>() {
        override fun areItemsTheSame(a: HardwareTestItem, b: HardwareTestItem) = a.id == b.id
        override fun areContentsTheSame(a: HardwareTestItem, b: HardwareTestItem) = a == b
    }
}
