package com.example.confelog.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.viewModels
import com.example.confelog.viewmodel.AuthViewModel
import android.content.Context
import com.example.confelog.ui.MainActivity
import com.example.confelog_ptbr.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val vm: AuthViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("confelog", Context.MODE_PRIVATE)
        if (prefs.contains("user_username")) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val pass = binding.edtPass.text.toString().trim()
            vm.login(username, pass) { ok, msg, user ->
                runOnUiThread {
                    if (ok && user != null) {
                        prefs.edit().putString("user_username", user.username).putString("user_role", user.role).apply()
                        Toast.makeText(this, "Bem-vindo, ${user.username}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
