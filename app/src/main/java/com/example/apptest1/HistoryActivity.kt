package com.example.apptest1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.tab_history

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.tab_scan -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.tab_history -> true

                R.id.tab_howto -> {
                    startActivity(Intent(this, HowToActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}
