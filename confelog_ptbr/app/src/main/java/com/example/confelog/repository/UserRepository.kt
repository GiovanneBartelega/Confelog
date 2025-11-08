package com.example.confelog.repository

import com.example.confelog.data.UserDao
import com.example.confelog.model.User

class UserRepository(private val userDao: UserDao) {
    suspend fun register(user: User): Long = userDao.insert(user)
    suspend fun findByUsername(username: String): User? = userDao.findByUsername(username)
    suspend fun all(): List<User> = userDao.getAll()
}
