package com.example.confelog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.confelog.model.*

// A versão da base de dados foi incrementada para 15 para forçar a recriação
@Database(entities = [User::class, Product::class, InventoryTransaction::class, Order::class, Cliente::class, Producao::class, Recipe::class, RecipeIngredient::class], version = 15, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun orderDao(): OrderDao
    abstract fun clienteDao(): ClienteDao
    abstract fun producaoDao(): ProducaoDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "conferlog.db"
                )
                // fallbackToDestructiveMigration irá apagar e recriar a base de dados
                // se a nova versão não tiver uma migração definida. Isto resolve o crash.
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
