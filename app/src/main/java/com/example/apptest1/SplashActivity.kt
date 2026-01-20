package com.example.apptest1.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.apptest1.MainActivity
import com.example.apptest1.R
import com.example.apptest1.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val hasOnboarded = prefs.getBoolean("onboarding_done", false)

            val nextActivity = if (hasOnboarded) {
                MainActivity::class.java
            } else {
                OnboardingActivity::class.java
            }

            startActivity(Intent(this, nextActivity))

            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )

            finish()

        }, 1500)
    }
}
