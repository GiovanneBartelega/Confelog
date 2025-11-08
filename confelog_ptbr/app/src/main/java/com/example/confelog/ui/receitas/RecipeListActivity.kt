package com.example.confelog.ui.receitas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.confelog_ptbr.databinding.ActivityRecipeListBinding

class RecipeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddRecipe.setOnClickListener {
            startActivity(Intent(this, RecipeCreateActivity::class.java))
        }
    }
}
