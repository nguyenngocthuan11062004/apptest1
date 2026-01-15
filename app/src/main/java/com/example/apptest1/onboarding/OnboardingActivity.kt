package com.example.apptest1.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.apptest1.MainActivity
import com.example.apptest1.databinding.ActivityOnboardingBinding
import com.example.apptest1.model.*
import com.example.apptest1.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var pages: List<OnboardingPage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pages = createPages()

        binding.viewPager.adapter = OnboardingPagerAdapter(this, pages)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // 🔥 INIT DOTS
        setupDots(pages.size)
        setActiveDot(0)

        updateCTA(0)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setActiveDot(position)
                    updateCTA(position)
                }
            }
        )

        binding.btnCta.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.lastIndex) {
                binding.viewPager.currentItem = current + 1
            } else {
                goToMain()
            }
        }
    }

    /**
     * CTA logic
     * - STATIC  -> enable
     * - OPTION  -> disable (UI only, chưa xử lý chọn option)
     */
    private fun updateCTA(position: Int) {
        val page = pages[position]

        binding.btnCta.text = page.ctaText
        binding.btnCta.visibility = View.VISIBLE

        val enabled = page.screenType != OnboardingScreenType.OPTION
        binding.btnCta.isEnabled = enabled
        binding.btnCta.alpha = if (enabled) 1f else 0.45f
    }

    /**
     * ===== DOT INDICATOR =====
     */

    private fun setupDots(total: Int) {
        val indicator = binding.dotIndicator
        indicator.removeAllViews()

        val dotSize = dpToPx(8)
        val dotMargin = dpToPx(6)

        repeat(total) { index ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    if (index > 0) marginStart = dotMargin
                }
                setBackgroundResource(R.drawable.bg_dot_inactive)
            }
            indicator.addView(dot)
        }
    }

    private fun setActiveDot(position: Int) {
        val indicator = binding.dotIndicator
        for (i in 0 until indicator.childCount) {
            indicator.getChildAt(i).setBackgroundResource(
                if (i == position)
                    R.drawable.bg_dot_active
                else
                    R.drawable.bg_dot_inactive
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * ===== NAVIGATION =====
     */

    private fun goToMain() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", true)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * ===== DATA (THÊM BAO NHIÊU MÀN = THÊM BAO NHIÊU DOT) =====
     */
    private fun createPages(): List<OnboardingPage> {
        return listOf(

            OnboardingPage(
                index = 0,
                screenType = OnboardingScreenType.STATIC,
                title = "EVERY 44 SECONDS",
                description = "Someone falls for a scam",
                ctaText = "I want protection now →",
                layoutStyle = StaticLayoutStyle.BIG_COUNTER_44
            ),

            OnboardingPage(
                index = 1,
                screenType = OnboardingScreenType.OPTION,
                title = "Have you ever received...",
                ctaText = "Next step →",
                options = listOf(
                    OnboardingOption(
                        id = "fake_account",
                        title = "Fake Account Alert",
                        subtitle = "Your account will be suspended if not verified"
                    ),
                    OnboardingOption(
                        id = "fake_package",
                        title = "Fake Package Notification",
                        subtitle = "Your delivery is pending additional payment"
                    ),
                    OnboardingOption(
                        id = "ezpass",
                        title = "EZPass Toll Payment",
                        subtitle = "Unpaid tolls require immediate action"
                    ),
                    OnboardingOption(
                        id = "fake_bank",
                        title = "Fake Bank Verification",
                        subtitle = "Unusual activity detected on your account"
                    ),
                    OnboardingOption(
                        id = "none",
                        title = "I've received none of these"
                    )
                )
            ),

            OnboardingPage(
                index = 2,
                screenType = OnboardingScreenType.OPTION,
                title = "How confident are you about\nspotting scams?",
                ctaText = "Continue to solution →",
                options = listOf(
                    OnboardingOption(
                        id = "very_confident",
                        title = "Very confident - I always know what to look for"
                    ),
                    OnboardingOption(
                        id = "somewhat_confident",
                        title = "Somewhat confident - But I still worry sometimes"
                    ),
                    OnboardingOption(
                        id = "not_very_confident",
                        title = "Not very confident - They're getting more sophisticated"
                    ),
                    OnboardingOption(
                        id = "concerned",
                        title = "Concerned - I know someone who's been scammed"
                    )
                )
            ),
            OnboardingPage(
                index = 3,
                screenType = OnboardingScreenType.STATIC,
                title = "Scammers are getting smarter",
                ctaText = "Show me how to stay safe →",
                layoutStyle = StaticLayoutStyle.INFO_CARDS,
                // dùng description như payload (hoặc bạn tách field riêng nếu muốn)
                description = null
            ),
            OnboardingPage(
                index = 4,
                screenType = OnboardingScreenType.STATIC,
                title = "Example Crypto Email Scam",
                ctaText = "Show me more examples →",
                layoutStyle = StaticLayoutStyle.EXAMPLE_SCAM
            ),
            OnboardingPage(
                index = 5,
                screenType = OnboardingScreenType.STATIC,
                title = "Real People, Real Protection",
                ctaText = "Get my protection now →",
                layoutStyle = StaticLayoutStyle.REAL_PROTECTION
            )
        )
    }
}
