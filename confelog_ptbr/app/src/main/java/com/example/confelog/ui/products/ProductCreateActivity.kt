package com.example.confelog.ui.products

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.Product
import com.example.confelog_ptbr.R
import com.example.confelog_ptbr.databinding.ActivityProductCreateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ProductCreateActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var binding: ActivityProductCreateBinding

    // Variáveis para os RadioGroups
    private lateinit var rgCategory: RadioGroup
    private lateinit var rgUnitType: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(applicationContext)

        // FORÇA O TIPO DE TECLADO CORRETO
        binding.edtName.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        binding.edtDescription.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        binding.edtPrice.inputType = InputType.TYPE_CLASS_NUMBER

        // Inicializa os RadioGroups usando findViewById para garantir
        rgCategory = findViewById(R.id.rg_category)
        rgUnitType = findViewById(R.id.rg_unit_type)

        setupCategoryListener()
        setupUnitTypeListener()
        setupPriceMask()

        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val name = binding.edtName.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val cleanString = binding.edtPrice.text.toString().replace(Regex("[^\\d]"), "")
        val price = (cleanString.toDoubleOrNull() ?: 0.0) / 100.0

        val qty = binding.edtQty.text.toString().toIntOrNull() ?: 0
        val unitsPerFardo = binding.edtUnitsPerFardo.text.toString().toIntOrNull()

        val category = when (rgCategory.checkedRadioButtonId) {
            R.id.rb_insumo -> "Insumo"
            R.id.rb_embalagem -> "Embalagem"
            else -> ""
        }

        val measurementValue = if (category == "Insumo") binding.edtMeasurementValue.text.toString().toDoubleOrNull() else null
        val measurementUnitRadioGroup = findViewById<RadioGroup>(R.id.rg_measurement_unit)
        val measurementUnit = if (category == "Insumo") {
            when (measurementUnitRadioGroup.checkedRadioButtonId) {
                R.id.rb_g -> "g"
                R.id.rb_ml -> "ml"
                else -> null
            }
        } else null

        val unitType = when (rgUnitType.checkedRadioButtonId) {
            R.id.rb_unidade -> "unidade"
            R.id.rb_fardos -> "fardos"
            else -> ""
        }

        if (name.isEmpty() || category.isEmpty() || unitType.isEmpty()) {
            Toast.makeText(this, "Nome, Categoria e Tipo de Unidade são obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        if (category == "Insumo" && (measurementValue == null || measurementUnit == null)) {
            Toast.makeText(this, "Medida (valor e unidade) é obrigatória para Insumos", Toast.LENGTH_SHORT).show()
            return
        }

        if (unitType == "fardos" && unitsPerFardo == null) {
            Toast.makeText(this, "Unidades por fardo é obrigatório quando o tipo é Fardos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.productDao().insert(Product(
                    name = name,
                    description = description,
                    price = price,
                    quantity = qty,
                    category = category,
                    unitType = unitType,
                    measurementValue = measurementValue,
                    measurementUnit = measurementUnit,
                    unitsPerFardo = unitsPerFardo
                ))
            }
            runOnUiThread {
                Toast.makeText(this@ProductCreateActivity, "Item salvo com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupCategoryListener() {
        rgCategory.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_insumo) {
                binding.containerMeasurement.visibility = View.VISIBLE
            } else {
                binding.containerMeasurement.visibility = View.GONE
            }
        }
    }

    private fun setupUnitTypeListener() {
        rgUnitType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_fardos) {
                binding.edtUnitsPerFardo.visibility = View.VISIBLE
            } else {
                binding.edtUnitsPerFardo.visibility = View.GONE
                binding.edtUnitsPerFardo.text.clear()
            }
        }
    }

    private fun setupPriceMask() {
        binding.edtPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) return

                binding.edtPrice.removeTextChangedListener(this)

                val cleanString = s.toString().replace(Regex("[^\\d]"), "")

                val parsed = if (cleanString.isEmpty()) 0.0 else cleanString.toDouble()
                val locale = Locale("pt", "BR")
                val formatted = NumberFormat.getCurrencyInstance(locale).format((parsed / 100))

                binding.edtPrice.setText(formatted)
                binding.edtPrice.setSelection(formatted.length)

                binding.edtPrice.addTextChangedListener(this)
            }
        })
    }
}
