package com.example.apptest1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HowToActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.tab_howto

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.tab_scan -> {
                    navigateTo(MainActivity::class.java)
                    true
                }

                R.id.tab_history -> {
                    navigateTo(HistoryActivity::class.java)
                    true
                }

                R.id.tab_howto -> true

                else -> false
            }
        }
    }

    private fun navigateTo(target: Class<out AppCompatActivity>) {
        val intent = Intent(this, target)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish() // 🔥 QUAN TRỌNG: không để stack chồng
    }
}
