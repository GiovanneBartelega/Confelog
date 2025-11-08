package com.example.confelog.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.Product
import com.example.confelog_ptbr.R
import com.example.confelog_ptbr.databinding.ActivityProductsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProductListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityProductsBinding
    private lateinit var listAdapter: ProductExpandableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)
        listAdapter = ProductExpandableListAdapter(this, emptyList())
        binding.listProducts.setAdapter(listAdapter)

        binding.btnAddProduct.setOnClickListener {
            startActivity(Intent(this, ProductCreateActivity::class.java))
        }

        lifecycleScope.launch {
            db.productDao().observeAll().collect { products ->
                listAdapter.updateData(products)
            }
        }
    }

    inner class ProductExpandableListAdapter(private val context: Context, private var productList: List<Product>) : BaseExpandableListAdapter() {

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
            val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)
            val product = getChild(groupPosition, childPosition)
            text1.text = product?.name
            text2.text = "Qtd: ${product?.quantity} ${product?.unitType} - R$ ${product?.price}"
            return view
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }
}
