package com.example.confelog.data

import androidx.room.*
import com.example.confelog.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>
}

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun findById(id: Int): Product?

    @Query("SELECT * FROM products ORDER BY name")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :q || '%' ORDER BY name")
    suspend fun search(q: String): List<Product>

    @Query("SELECT * FROM products WHERE category = 'Produto Final'")
    suspend fun getFinalProducts(): List<Product>
}

@Dao
interface InventoryDao {
    @Insert
    suspend fun insert(tx: InventoryTransaction): Long

    @Query("SELECT * FROM inventory_transactions WHERE productId = :productId ORDER BY date DESC")
    suspend fun listForProduct(productId: Int): List<InventoryTransaction>
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order): Long

    @Delete
    suspend fun delete(order: Order)

    @Query("SELECT * FROM orders ORDER BY dateMillis")
    fun observeAll(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun findById(id: Int): Order?
}

@Dao
interface ClienteDao {
    @Insert
    suspend fun insert(cliente: Cliente): Long

    @Query("SELECT * FROM clientes ORDER BY nome")
    fun observeAll(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE nome LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Cliente>
}

@Dao
interface ProducaoDao {
    @Insert
    suspend fun insert(producao: Producao): Long

    @Update
    suspend fun update(producao: Producao)

    @Query("SELECT * FROM producao ORDER BY startDate DESC")
    fun observeAll(): Flow<List<Producao>>
}

@Dao
interface RecipeDao {
    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert
    suspend fun insertIngredients(ingredients: List<RecipeIngredient>)

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: Int): List<RecipeIngredient>
}
