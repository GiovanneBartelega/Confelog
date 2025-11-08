package com.example.confelog.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.InventoryTransaction
import com.example.confelog.model.Product
import com.example.confelog_ptbr.databinding.ActivityStockReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class StockReportActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityStockReportBinding
    private lateinit var listAdapter: StockExpandableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)
        listAdapter = StockExpandableListAdapter(this, emptyList())
        binding.listStock.setAdapter(listAdapter)

        binding.listStock.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val product = listAdapter.getChild(groupPosition, childPosition)
            product?.let { showStockWriteOffDialog(it) }
            true
        }

        lifecycleScope.launch {
            db.productDao().observeAll().collect { products ->
                listAdapter.updateData(products)
            }
        }
    }

    private fun showStockWriteOffDialog(product: Product) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Dar Baixa no Estoque")
        builder.setMessage("Produto: ${product.name}\nEstoque atual: ${product.quantity}")

        val input = EditText(this)
        input.hint = "Quantidade a remover"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Confirmar") { dialog, _ ->
            val quantityToRemove = input.text.toString().toIntOrNull()
            if (quantityToRemove == null || quantityToRemove <= 0) {
                Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (quantityToRemove > product.quantity) {
                Toast.makeText(this, "Quantidade maior que o estoque atual", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            performStockWriteOff(product, quantityToRemove)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun performStockWriteOff(product: Product, quantityToRemove: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val updatedProduct = product.copy(quantity = product.quantity - quantityToRemove)
                db.productDao().update(updatedProduct)

                val transaction = InventoryTransaction(
                    productId = product.id,
                    quantityChange = -quantityToRemove,
                    date = Date(),
                    reason = "Baixa manual"
                )
                db.inventoryDao().insert(transaction)
            }
            runOnUiThread {
                Toast.makeText(this@StockReportActivity, "Baixa de estoque realizada com sucesso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class StockExpandableListAdapter(private val context: Context, private var productList: List<Product>) : BaseExpandableListAdapter() {
        private var categories: List<String> = emptyList()
        private var productMap: Map<String, List<Product>> = emptyMap()

        init {
            updateData(productList)
        }

        fun updateData(newProductList: List<Product>) {
            this.productList = newProductList
            productMap = newProductList.groupBy { it.category }
            categories = productMap.keys.sorted()
            notifyDataSetChanged()
        }

        override fun getGroupCount(): Int = categories.size
        override fun getChildrenCount(groupPosition: Int): Int = productMap[categories[groupPosition]]?.size ?: 0
        override fun getGroup(groupPosition: Int): String = categories[groupPosition]
        override fun getChild(groupPosition: Int, childPosition: Int): Product? = productMap[categories[groupPosition]]?.get(childPosition)
        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
        override fun hasStableIds(): Boolean = false

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = getGroup(groupPosition)
            return view
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            val product = getChild(groupPosition, childPosition)
            textView.text = "${product?.name} - Estoque: ${product?.quantity} ${product?.unitType}"
            return view
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }
}
