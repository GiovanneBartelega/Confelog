package com.example.confelog.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import com.example.confelog.ui.auth.LoginActivity
import com.example.confelog.ui.clientes.ClienteListActivity
import com.example.confelog.ui.orders.OrderListActivity
import com.example.confelog.ui.producao.ProducaoListActivity
import com.example.confelog.ui.products.ProductListActivity
import com.example.confelog.ui.receitas.RecipeListActivity
import com.example.confelog.ui.users.UserManagementActivity
import com.example.confelog_ptbr.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("confelog", Context.MODE_PRIVATE)
        val userRole = prefs.getString("user_role", null)

        if (userRole == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        if (userRole == "admin") {
            binding.btnManageUsers.visibility = View.VISIBLE
        }

        binding.btnProducao.setOnClickListener {
            startActivity(Intent(this, ProducaoListActivity::class.java))
        }
        binding.btnRecipes.setOnClickListener {
            startActivity(Intent(this, RecipeListActivity::class.java))
        }
        binding.btnProducts.setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }
        binding.btnOrders.setOnClickListener {
            startActivity(Intent(this, OrderListActivity::class.java))
        }
        binding.btnClientes.setOnClickListener {
            startActivity(Intent(this, ClienteListActivity::class.java))
        }
        binding.btnStockReport.setOnClickListener {
            startActivity(Intent(this, StockReportActivity::class.java))
        }
        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
