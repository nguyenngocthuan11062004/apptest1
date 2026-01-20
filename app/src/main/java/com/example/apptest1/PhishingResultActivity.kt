package com.example.apptest1

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptest1.model.GeminiApi
import org.json.JSONObject
import java.util.Locale

class PhishingResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phishing_result)

        val resultText = findViewById<TextView>(R.id.resultText)
        val confidenceText = findViewById<TextView>(R.id.confidenceText)
        val detailText = findViewById<TextView>(R.id.detailText)
        val resultCard = findViewById<LinearLayout>(R.id.resultCard)
        val resultIcon = findViewById<ImageView>(R.id.resultIcon)
        val confidenceBar = findViewById<ProgressBar>(R.id.confidenceBar)

        val analyzeButton = findViewById<Button>(R.id.btnAnalyzeAI)
        val continueButton = findViewById<Button>(R.id.btnContinueCheck)

        val aiContainer = findViewById<LinearLayout>(R.id.aiExplanationContainer)
        val aiExplanationText = findViewById<TextView>(R.id.aiExplanationText)

        val rawResult = intent.getStringExtra("PHISHING_RESULT")
        val emailText = intent.getStringExtra("OCR_TEXT") ?: ""

        Log.d("PhishingResultActivity", "Raw result = $rawResult")
        Log.d("PhishingResultActivity", "OCR text = $emailText")

        if (rawResult.isNullOrBlank()) {
            resultText.text = "UNKNOWN"
            detailText.text = "No data available"
            analyzeButton.isEnabled = false
            return
        }

        var finalPrediction = ""
        var finalConfidence = 0.0

        try {
            val json = JSONObject(rawResult)

            finalPrediction = json.optString("prediction")
            finalConfidence = json.optDouble("confidence", 0.0)

            val isPhishing = finalPrediction == "phishing_email"

            val confidencePercentInt = (finalConfidence * 100).toInt()
            val confidencePercentText =
                String.format(Locale.US, "%.2f%%", finalConfidence * 100)

            confidenceText.text = "Confidence: $confidencePercentText"
            confidenceBar.progress = confidencePercentInt

            if (isPhishing) {
                resultText.text = "PHISHING"
                resultText.setTextColor(0xFFFF3B30.toInt())

                detailText.text =
                    "This email is highly likely to be a phishing attempt."

                resultIcon.setImageResource(R.drawable.ic_warning_v2)

                confidenceBar.progressTintList =
                    ColorStateList.valueOf(0xFFFF3B30.toInt())

                resultCard.setBackgroundResource(
                    R.drawable.bg_result_gradient
                )

            } else {
                resultText.text = "SAFE"
                resultText.setTextColor(0xFF34C759.toInt())

                detailText.text =
                    "This email appears to be legitimate."

                resultIcon.setImageResource(R.drawable.ic_safe)

                confidenceBar.progressTintList =
                    ColorStateList.valueOf(0xFF34C759.toInt())

                resultCard.setBackgroundResource(
                    R.drawable.bg_result_gradient_safe
                )
            }

        } catch (e: Exception) {
            Log.e("PhishingResultActivity", "Parse error", e)
            resultText.text = "ERROR"
            detailText.text = "Failed to parse result"
            analyzeButton.isEnabled = false
            return
        }
        continueButton.setOnClickListener {
            finish()
        }

        analyzeButton.setOnClickListener {

            if (emailText.isBlank()) {
                aiContainer.visibility = View.VISIBLE
                aiExplanationText.text =
                    "No email content available for AI analysis."
                return@setOnClickListener
            }

            analyzeButton.isEnabled = false
            analyzeButton.text = "Analyzing..."

            val prompt = GeminiPromptBuilder.buildExplainPrompt(
                emailText = emailText,
                prediction = finalPrediction,
                confidence = finalConfidence
            )

            GeminiApi.generate(prompt) { success, result ->
                runOnUiThread {
                    analyzeButton.isEnabled = true
                    analyzeButton.text = "Analyze with AI"

                    aiContainer.visibility = View.VISIBLE
                    aiExplanationText.text =
                        if (success) result
                        else "AI analysis failed. Please try again."
                }
            }
        }
    }
}
