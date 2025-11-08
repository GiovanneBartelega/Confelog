package com.example.confelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.User
import com.example.confelog.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application): AndroidViewModel(application) {
    private val repo: UserRepository

    init {
        val db = AppDatabase.getInstance(application)
        repo = UserRepository(db.userDao())
    }

    fun registrar(username: String, senha: String, papel: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existe = withContext(Dispatchers.IO) { repo.findByUsername(username) }
            if (existe != null) {
                onResult(false, "Usuário já cadastrado")
                return@launch
            }
            val id = withContext(Dispatchers.IO) {
                repo.register(User(username = username, password = senha, role = papel))
            }
            onResult(id > 0, if (id > 0) "Registrado com sucesso" else "Erro ao registrar")
        }
    }

    fun login(username: String, senha: String, onResult: (Boolean, String, User?) -> Unit) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { repo.findByUsername(username) }
            if (user == null) {
                onResult(false, "Usuário não encontrado", null)
            } else if (user.password != senha) {
                onResult(false, "Senha incorreta", null)
            } else {
                onResult(true, "OK", user)
            }
        }
    }
}
