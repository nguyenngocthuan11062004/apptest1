package com.example.apptest1.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.apptest1.MainActivity
import com.example.apptest1.databinding.ActivityOnboardingBinding
import com.example.apptest1.model.OnboardingPage

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingPagerAdapter
    private lateinit var pages: List<OnboardingPage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pages = createPages()

        adapter = OnboardingPagerAdapter(this, pages)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        updateCTA(0)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
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

    private fun updateCTA(position: Int) {
        binding.btnCta.text = pages[position].ctaText
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun createPages(): List<OnboardingPage> {
        return (1..10).map { index ->
            OnboardingPage(
                index = index,
                title = "Onboarding Screen $index",
                description = "This is onboarding screen number $index",
                ctaText = if (index == 10) "Finish" else "Next"
            )
        }
    }
}
