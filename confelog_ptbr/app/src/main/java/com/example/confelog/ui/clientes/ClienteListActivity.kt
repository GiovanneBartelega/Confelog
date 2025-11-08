package com.example.confelog.ui.clientes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.confelog.data.AppDatabase
import com.example.confelog_ptbr.databinding.ActivityClienteListBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ClienteListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var binding: ActivityClienteListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        binding.listClientes.adapter = adapter

        binding.btnAddCliente.setOnClickListener {
            startActivity(Intent(this, ClienteCreateActivity::class.java))
        }

        lifecycleScope.launch {
            db.clienteDao().observeAll().collect { clientes ->
                val clienteStrings = clientes.map { "${it.nome} - ${it.cpf}" }
                adapter.clear()
                if (clienteStrings.isEmpty()) {
                    adapter.add("Nenhum cliente cadastrado")
                } else {
                    adapter.addAll(clienteStrings)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}
