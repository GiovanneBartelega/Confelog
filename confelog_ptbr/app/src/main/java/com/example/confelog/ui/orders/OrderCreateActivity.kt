package com.example.confelog.ui.orders

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.*
import com.example.confelog_ptbr.databinding.ActivityOrderCreateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OrderCreateActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityOrderCreateBinding
    private var clientes: List<Cliente> = emptyList()
    private var finalProducts: List<Product> = emptyList()
    private var selectedProduct: Product? = null
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)

        setupClientAutoComplete()
        setupProductAutoComplete()
        setupDatePicker()

        binding.btnSaveOrder.setOnClickListener {
            if (validateInputs()) {
                saveOrder()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Reinicia os erros
        binding.tilClient.error = null
        binding.tilProduct.error = null
        binding.tilAddress.error = null
        binding.tilDate.error = null
        binding.tilQty.error = null

        val clientName = binding.edtClient.text.toString().trim()
        val address = binding.edtAddress.text.toString().trim()
        val date = binding.edtDate.text.toString().trim()
        val qty = binding.edtQty.text.toString().toIntOrNull()

        if (clientName.isEmpty()) {
            binding.tilClient.error = "Cliente é obrigatório"
            return false
        }
        if (selectedProduct == null) {
            binding.tilProduct.error = "Produto é obrigatório"
            return false
        }
        if (address.isEmpty()) {
            binding.tilAddress.error = "Endereço é obrigatório"
            return false
        }
        if (date.isEmpty()) {
            binding.tilDate.error = "Data é obrigatória"
            return false
        }
        if (qty == null || qty <= 0) {
            binding.tilQty.error = "Quantidade deve ser maior que zero"
            return false
        }

        return true
    }

    private fun saveOrder() {
        val clientName = binding.edtClient.text.toString().trim()
        val address = binding.edtAddress.text.toString().trim()
        val qty = binding.edtQty.text.toString().toInt()

        lifecycleScope.launch {
            try {
                db.withTransaction {
                    val order = Order(
                        clientName = clientName,
                        productId = selectedProduct!!.id,
                        productName = selectedProduct!!.name,
                        quantity = qty,
                        dateMillis = calendar.timeInMillis,
                        address = address,
                        value = selectedProduct!!.price
                    )
                    val orderId = db.orderDao().insert(order)

                    val producao = Producao(
                        orderId = orderId.toInt(),
                        productId = selectedProduct!!.id,
                        productName = selectedProduct!!.name,
                        quantity = qty,
                        status = "Pendente",
                        startDate = null,
                        completionDate = null
                    )
                    db.producaoDao().insert(producao)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderCreateActivity, "Encomenda e Ordem de Produção criadas", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OrderCreateActivity, "Erro ao salvar encomenda: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        val dateClickListener = View.OnClickListener {
            DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.edtDate.setOnClickListener(dateClickListener)
        binding.tilDate.setEndIconOnClickListener(dateClickListener)
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.edtDate.setText(sdf.format(calendar.time))
    }

    private fun setupClientAutoComplete() {
        lifecycleScope.launch(Dispatchers.IO) {
            clientes = db.clienteDao().search("")
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@OrderCreateActivity, android.R.layout.simple_dropdown_item_1line, clientes.map { it.nome })
                binding.edtClient.setAdapter(adapter)
            }
        }

        binding.edtClient.setOnItemClickListener { parent, _, position, _ ->
            val selectedCliente = clientes.find { it.nome == parent.getItemAtPosition(position).toString() }
            // CORRIGIDO: Usa os campos de endereço separados para construir o endereço completo
            selectedCliente?.let { binding.edtAddress.setText("${it.rua}, ${it.numero} - ${it.bairro}") }
        }
    }

    private fun setupProductAutoComplete() {
        lifecycleScope.launch(Dispatchers.IO) {
            finalProducts = db.productDao().getFinalProducts()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@OrderCreateActivity, android.R.layout.simple_dropdown_item_1line, finalProducts.map { it.name })
                binding.edtProduct.setAdapter(adapter)
            }
        }

        binding.edtProduct.setOnItemClickListener { parent, _, position, _ ->
            val selected = finalProducts.find { it.name == parent.getItemAtPosition(position).toString() }
            selectedProduct = selected
            selected?.let { binding.edtValue.setText(it.price.toString()) }
        }
    }
}
