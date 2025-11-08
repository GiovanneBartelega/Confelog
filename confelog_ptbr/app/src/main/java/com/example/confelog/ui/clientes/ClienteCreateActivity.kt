package com.example.confelog.ui.clientes

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.Cliente
import com.example.confelog_ptbr.databinding.ActivityClienteCreateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteCreateActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityClienteCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)

        // Define os tipos de teclado corretos
        binding.edtNome.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        binding.edtRua.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        binding.edtBairro.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        binding.edtNumero.inputType = InputType.TYPE_CLASS_NUMBER
        binding.edtCpf.inputType = InputType.TYPE_CLASS_PHONE // Teclado de telefone para permitir . e -
        binding.edtTelefone.inputType = InputType.TYPE_CLASS_PHONE

        setupMasks()

        binding.btnSaveCliente.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val cpf = binding.edtCpf.text.toString()
            val rua = binding.edtRua.text.toString().trim()
            val bairro = binding.edtBairro.text.toString().trim()
            val numero = binding.edtNumero.text.toString().trim()
            val telefone = binding.edtTelefone.text.toString()

            // Validação com os campos corretos
            if (nome.isEmpty() || cpf.length < 14 || rua.isEmpty() || bairro.isEmpty() || numero.isEmpty() || telefone.length < 14) { 
                Toast.makeText(this, "Todos os campos são obrigatórios", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    // Usa o construtor correto para Cliente, com os campos de endereço separados
                    db.clienteDao().insert(Cliente(nome = nome, cpf = cpf, rua = rua, bairro = bairro, numero = numero, telefone = telefone))
                }
                runOnUiThread {
                    Toast.makeText(this@ClienteCreateActivity, "Cliente salvo com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupMasks() {
        // Máscara para CPF (000.000.000-00) - LÓGICA CORRIGIDA E SEGURA
        binding.edtCpf.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (isUpdating) return
                isUpdating = true

                val cleanString = s.toString().filter { it.isDigit() }
                val truncated = if (cleanString.length > 11) cleanString.substring(0, 11) else cleanString

                val formatted = buildString {
                    var i = 0
                    while (i < truncated.length) {
                        when (i) {
                            3, 6 -> append('.')
                            9 -> append('-')
                        }
                        append(truncated[i])
                        i++
                    }
                }

                s.replace(0, s.length, formatted)
                binding.edtCpf.setSelection(s.length)

                isUpdating = false
            }
        })

        // Máscara para Telefone - LÓGICA CORRIGIDA E SEGURA
        binding.edtTelefone.addTextChangedListener(object : TextWatcher {
             private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (isUpdating) return
                isUpdating = true

                val cleanString = s.toString().filter { it.isDigit() }
                val truncated = if (cleanString.length > 11) cleanString.substring(0, 11) else cleanString

                val formatted = buildString {
                    when {
                        truncated.length >= 11 -> append("(${truncated.substring(0, 2)}) ${truncated.substring(2, 7)}-${truncated.substring(7)}")
                        truncated.length >= 7 -> append("(${truncated.substring(0, 2)}) ${truncated.substring(2, 6)}-${truncated.substring(6)}")
                        truncated.length >= 3 -> append("(${truncated.substring(0, 2)}) ${truncated.substring(2)}")
                        truncated.isNotEmpty() -> append("(").append(truncated)
                    }
                }
                s.replace(0, s.length, formatted)
                binding.edtTelefone.setSelection(s.length)
                isUpdating = false
            }
        })
    }
}
