package com.example.confelog.ui.users

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.ui.auth.RegisterActivity
import com.example.confelog_ptbr.databinding.ActivityUserManagementBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserManagementActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        binding.listUsers.adapter = adapter

        binding.btnAddUser.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) { db.userDao().getAll() }
            val userStrings = users.map { "${it.username} - (${it.role})" }
            adapter.clear()
            adapter.addAll(userStrings)
            adapter.notifyDataSetChanged()
        }
    }
}
