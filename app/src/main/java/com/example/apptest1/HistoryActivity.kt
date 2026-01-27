package com.example.apptest1

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import  com.example.apptest1.model.HistoryItem
import com.example.apptest1.HistoryStorage

class HistoryActivity : AppCompatActivity() {

    // ✅ KHAI BÁO ADAPTER Ở CẤP CLASS
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // ======================
        // RecyclerView setup
        // ======================
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        // ======================
        // Bottom Navigation
        // ======================
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

    override fun onResume() {
        super.onResume()

        try {
            val list = HistoryStorage.getAll(this)

            android.util.Log.d("HistoryActivity", "History size = ${list.size}")

            list.forEachIndexed { index, item ->
                android.util.Log.d(
                    "HistoryActivity",
                    "ITEM#$index result=${item.result.take(50)} imagePath=${item.imagePath}"
                )
            }

            if (list.isEmpty()) {
                findViewById<View>(R.id.emptyStateLayout).visibility = View.VISIBLE
                findViewById<View>(R.id.historyRecyclerView).visibility = View.GONE
            } else {
                findViewById<View>(R.id.emptyStateLayout).visibility = View.GONE
                findViewById<View>(R.id.historyRecyclerView).visibility = View.VISIBLE
                adapter.update(list)
            }

        } catch (e: Exception) {
            android.util.Log.e(
                "HistoryActivity",
                "🔥 CRASH IN onResume",
                e
            )
        }
    }
}
