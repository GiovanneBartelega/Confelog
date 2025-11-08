package com.example.confelog.ui.orders

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.InventoryTransaction
import com.example.confelog.model.Order
import com.example.confelog_ptbr.databinding.ActivityOrdersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class OrderListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var binding: ActivityOrdersBinding
    private var orders: List<Order> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        binding.listOrders.adapter = adapter

        binding.btnAddOrder.setOnClickListener {
            startActivity(Intent(this, OrderCreateActivity::class.java))
        }

        binding.listOrders.setOnItemLongClickListener { _, _, position, _ ->
            val order = orders[position]
            showDeleteConfirmationDialog(order)
            true
        }

        lifecycleScope.launch {
            db.orderDao().observeAll().collect { orderList ->
                orders = orderList
                val orderStrings = orders.map { 
                    "Cliente: ${it.clientName}\nProduto: ${it.productName} - Qtd: ${it.quantity}\nEndereço: ${it.address} - Valor: R$${it.value}"
                }
                adapter.clear()
                if (orderStrings.isEmpty()) {
                    adapter.add("Nenhuma encomenda cadastrada")
                } else {
                    adapter.addAll(orderStrings)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showDeleteConfirmationDialog(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Encomenda")
            .setMessage("Tem certeza que deseja excluir esta encomenda? O produto retornará ao estoque.")
            .setPositiveButton("Excluir") { _, _ ->
                deleteOrderAndReturnStock(order)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteOrderAndReturnStock(order: Order) {
        lifecycleScope.launch {
            try {
                db.withTransaction {
                    val product = db.productDao().findById(order.productId)
                    if (product != null) {
                        // 1. Devolver o produto ao estoque
                        val updatedProduct = product.copy(quantity = product.quantity + order.quantity)
                        db.productDao().update(updatedProduct)

                        // 2. Registrar a transação de devolução
                        val transaction = InventoryTransaction(
                            productId = product.id,
                            quantityChange = order.quantity, // positivo para entrada
                            date = Date(),
                            reason = "Exclusão de Encomenda"
                        )
                        db.inventoryDao().insert(transaction)

                        // 3. Excluir a encomenda
                        db.orderDao().delete(order)
                    } else {
                        // Se o produto não existe mais, apenas exclui a encomenda
                        db.orderDao().delete(order)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderListActivity, "Encomenda excluída e estoque atualizado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderListActivity, "Erro ao excluir encomenda: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
