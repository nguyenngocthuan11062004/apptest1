package com.example.apptest1

import android.content.Intent
import android.os.Bundle
import android.util.Log // Thêm import Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PhishingResultActivity : AppCompatActivity() {

    private lateinit var phishingResultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phishing_result)

        phishingResultText = findViewById(R.id.phishingResultText)

        // Lấy kết quả từ Intent
        val phishingResult = intent.getStringExtra("PHISHING_RESULT")

        // In log để kiểm tra kết quả nhận được
        Log.d("PhishingResultActivity", "Received phishing result: $phishingResult")

        phishingResult?.let {
            phishingResultText.text = it
        } ?: run {
            phishingResultText.text = "No phishing result available"
        }
    }
}
