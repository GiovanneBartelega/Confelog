package com.example.confelog.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.viewModels
import com.example.confelog.viewmodel.AuthViewModel
import com.example.confelog_ptbr.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private val vm: AuthViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val pass = binding.edtPass.text.toString().trim()
            var role = binding.edtRole.text.toString().trim()
            if (role.isEmpty()) role = "cliente"
            vm.registrar(username, pass, role) { ok, msg ->
                runOnUiThread {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    if (ok) finish()
                }
            }
        }
    }
}
