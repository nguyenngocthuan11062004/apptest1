package com.example.apptest1

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.apptest1.model.GeminiApi

class AiAssistantView(private val root: View) {

    private val input = root.findViewById<EditText>(R.id.aiInput)
    private val askButton = root.findViewById<Button>(R.id.btnAskAI)
    private val answerText = root.findViewById<TextView>(R.id.aiAnswerText)

    fun bind() {
        askButton.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            askButton.isEnabled = false
            askButton.text = "Thinking..."
            answerText.visibility = View.GONE

            val prompt = GeminiPromptBuilder.buildAskPrompt(text)

            GeminiApi.generate(prompt) { success, result ->
                (root.context as Activity).runOnUiThread {
                    askButton.isEnabled = true
                    askButton.text = "Ask AI"
                    answerText.visibility = View.VISIBLE
                    answerText.text =
                        if (success) result
                        else "Failed to get response. Please try again."
                }
            }
        }
    }
}
