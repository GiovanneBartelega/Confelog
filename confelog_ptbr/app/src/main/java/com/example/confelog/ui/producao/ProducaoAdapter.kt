package com.example.confelog.ui.producao

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.confelog.model.Producao
import com.example.confelog_ptbr.databinding.ItemProducaoBinding

class ProducaoAdapter(private val onProducaoClicked: (Producao) -> Unit) : ListAdapter<Producao, ProducaoAdapter.ProducaoViewHolder>(ProducaoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProducaoViewHolder {
        val binding = ItemProducaoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProducaoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProducaoViewHolder, position: Int) {
        val producao = getItem(position)
        holder.bind(producao)
        holder.itemView.setOnClickListener {
            onProducaoClicked(producao)
        }
    }

    class ProducaoViewHolder(private val binding: ItemProducaoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(producao: Producao) {
            binding.tvProductName.text = producao.productName
            binding.tvQuantity.text = "Qtd: ${producao.quantity}"
            binding.tvStatus.text = "Status: ${producao.status}"
        }
    }
}

class ProducaoDiffCallback : DiffUtil.ItemCallback<Producao>() {
    override fun areItemsTheSame(oldItem: Producao, newItem: Producao): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Producao, newItem: Producao): Boolean {
        return oldItem == newItem
    }
}
