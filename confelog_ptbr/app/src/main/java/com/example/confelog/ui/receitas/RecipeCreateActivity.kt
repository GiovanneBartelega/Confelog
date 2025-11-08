package com.example.confelog.ui.receitas

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.Product
import com.example.confelog.model.Recipe
import com.example.confelog.model.RecipeIngredient
import com.example.confelog_ptbr.R
import com.example.confelog_ptbr.databinding.ActivityRecipeCreateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class RecipeCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeCreateBinding
    private lateinit var db: AppDatabase
    private var insumos: List<Product> = emptyList()
    private val ingredientViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)

        // FORÇA O TIPO DE TECLADO CORRETO
        binding.edtFinalProductName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        binding.edtFinalProductPrice.inputType = InputType.TYPE_CLASS_NUMBER

        // Desativa o botão de adicionar ingrediente no início
        binding.btnAddIngredient.isEnabled = false

        setupPriceMask()

        lifecycleScope.launch(Dispatchers.IO) {
            insumos = db.productDao().search("") // Carrega todos os insumos e embalagens
            withContext(Dispatchers.Main) {
                // Ativa o botão depois que a lista de insumos for carregada
                binding.btnAddIngredient.isEnabled = true
            }
        }

        binding.btnAddIngredient.setOnClickListener {
            addIngredientView()
        }

        binding.btnSaveRecipe.setOnClickListener {
            saveRecipeAndProduct()
        }
    }

    private fun addIngredientView() {
        val inflater = LayoutInflater.from(this)
        val ingredientView = inflater.inflate(R.layout.item_ingredient_input, binding.containerIngredients, false)

        val insumoAutoComplete = ingredientView.findViewById<AutoCompleteTextView>(R.id.ac_insumo)
        // FORÇA O TIPO DE TECLADO CORRETO NO CAMPO DE INGREDIENTE
        insumoAutoComplete.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        
        val insumoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, insumos.map { it.name })
        insumoAutoComplete.setAdapter(insumoAdapter)

        binding.containerIngredients.addView(ingredientView)
        ingredientViews.add(ingredientView)
    }

    private fun saveRecipeAndProduct() {
        val finalProductName = binding.edtFinalProductName.text.toString().trim()
        val cleanString = binding.edtFinalProductPrice.text.toString().replace(Regex("[^\\d]"), "")
        val finalProductPrice = (cleanString.toDoubleOrNull() ?: 0.0) / 100.0

        if (finalProductName.isEmpty() || finalProductPrice == 0.0) {
            Toast.makeText(this, "Nome e preço do produto final são obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredients = mutableListOf<RecipeIngredient>()
        for (view in ingredientViews) {
            val insumoName = view.findViewById<AutoCompleteTextView>(R.id.ac_insumo).text.toString().trim()
            val quantity = view.findViewById<EditText>(R.id.edt_ingredient_qty).text.toString().toDoubleOrNull()

            if (insumoName.isEmpty() || quantity == null || quantity <= 0.0) {
                Toast.makeText(this, "Todos os ingredientes devem ter um nome e uma quantidade válida.", Toast.LENGTH_LONG).show()
                return
            }

            val insumo = insumos.find { it.name.equals(insumoName, ignoreCase = true) }

            if (insumo == null) {
                // ERRO ESPECÍFICO: Aponta qual ingrediente não foi encontrado
                Toast.makeText(this, "O ingrediente '$insumoName' não foi encontrado. Verifique se o nome está correto e se ele está cadastrado como um Insumo.", Toast.LENGTH_LONG).show()
                return
            }
            ingredients.add(RecipeIngredient(recipeId = 0, ingredientId = insumo.id, quantityNeeded = quantity))
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "A receita precisa de pelo menos um ingrediente", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                db.withTransaction {
                    val newProduct = Product(
                        name = finalProductName,
                        price = finalProductPrice,
                        category = "Produto Final",
                        quantity = 0,
                        description = null,
                        unitType = "unidade",
                        measurementValue = null,
                        measurementUnit = null,
                        unitsPerFardo = null
                    )
                    val newProductId = db.productDao().insert(newProduct).toInt()

                    val newRecipe = Recipe(finalProductId = newProductId, name = "Receita de $finalProductName")
                    val newRecipeId = db.recipeDao().insertRecipe(newRecipe).toInt()

                    val ingredientsWithRecipeId = ingredients.map { it.copy(recipeId = newRecipeId) }
                    db.recipeDao().insertIngredients(ingredientsWithRecipeId)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecipeCreateActivity, "Produto Final e Receita salvos!", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecipeCreateActivity, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupPriceMask() {
        binding.edtFinalProductPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) return

                binding.edtFinalProductPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(Regex("[^\\d]"), "")

                val parsed = if (cleanString.isEmpty()) 0.0 else cleanString.toDouble()
                val locale = Locale("pt", "BR")
                val formatted = NumberFormat.getCurrencyInstance(locale).format((parsed / 100))

                binding.edtFinalProductPrice.setText(formatted)
                binding.edtFinalProductPrice.setSelection(formatted.length)

                binding.edtFinalProductPrice.addTextChangedListener(this)
            }
        })
    }
}
