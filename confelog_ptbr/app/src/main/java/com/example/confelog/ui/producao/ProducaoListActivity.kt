package com.example.confelog.ui.producao

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.withTransaction
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.InventoryTransaction
import com.example.confelog.model.Producao
import com.example.confelog_ptbr.databinding.ActivityProducaoListBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date

class ProducaoListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProducaoListBinding
    private lateinit var db: AppDatabase
    private lateinit var producaoAdapter: ProducaoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProducaoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)

        setupRecyclerView()

        lifecycleScope.launch {
            db.producaoDao().observeAll().collect { producaoList ->
                producaoAdapter.submitList(producaoList)
            }
        }
    }

    private fun setupRecyclerView() {
        producaoAdapter = ProducaoAdapter { producao ->
            showStatusChangeDialog(producao)
        }
        binding.rvProducao.apply {
            adapter = producaoAdapter
            layoutManager = LinearLayoutManager(this@ProducaoListActivity)
        }
    }

    private fun showStatusChangeDialog(producao: Producao) {
        val nextStatus = when (producao.status) {
            "Pendente" -> "Em produção"
            "Em produção" -> "Concluída"
            else -> null
        }

        if (nextStatus == null) return

        AlertDialog.Builder(this)
            .setTitle("Mudar Status da Produção")
            .setMessage("Deseja alterar o status de '${producao.status}' para '$nextStatus'?")
            .setPositiveButton("Sim") { _, _ ->
                updateProducaoStatus(producao, nextStatus)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun updateProducaoStatus(producao: Producao, newStatus: String) {
        lifecycleScope.launch {
            db.withTransaction {
                val updatedProducao = producao.copy(status = newStatus)
                if (newStatus == "Em produção") {
                    updatedProducao.startDate = Date()
                } else if (newStatus == "Concluída") {
                    updatedProducao.completionDate = Date()

                    // Incrementa o estoque do produto final
                    val product = db.productDao().findById(producao.productId)
                    if (product != null) {
                        val updatedProduct = product.copy(quantity = product.quantity + producao.quantity)
                        db.productDao().update(updatedProduct)
                        
                        val transaction = InventoryTransaction(
                            productId = product.id,
                            quantityChange = producao.quantity, // positivo para entrada
                            date = Date(),
                            reason = "Produção Concluída"
                        )
                        db.inventoryDao().insert(transaction)
                    }
                }
                db.producaoDao().update(updatedProducao)
            }
        }
    }
}
