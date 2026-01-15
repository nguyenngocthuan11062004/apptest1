package com.example.apptest1.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apptest1.MainActivity
import com.example.apptest1.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isFirstOpen = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("onboarding_done", false)

        val nextIntent = if (isFirstOpen) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }

        startActivity(nextIntent)
        finish()
    }
}
