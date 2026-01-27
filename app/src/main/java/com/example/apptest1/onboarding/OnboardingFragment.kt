package com.example.apptest1.onboarding

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.apptest1.R
import com.example.apptest1.model.*
import androidx.core.text.HtmlCompat

class OnboardingFragment : Fragment() {

    private lateinit var page: OnboardingPage

    // chọn 1 option (lưu local trong fragment)
    private var selectedOptionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page = requireArguments().getSerializable(ARG_PAGE) as OnboardingPage
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return when (page.screenType) {

            OnboardingScreenType.STATIC -> {
                val layoutRes = when (page.layoutStyle) {
                    StaticLayoutStyle.BIG_COUNTER_44 ->
                        R.layout.fragment_onboarding_44_seconds

                    StaticLayoutStyle.INFO_CARDS ->
                        R.layout.fragment_onboarding_info_cards

                    StaticLayoutStyle.NORMAL ->
                        R.layout.fragment_onboarding_static
                    StaticLayoutStyle.EXAMPLE_SCAM ->
                        R.layout.fragment_onboarding_example_scam
                    StaticLayoutStyle.REAL_PROTECTION ->
                        R.layout.fragment_onboarding_real_protection
                }

                val view = inflater.inflate(layoutRes, container, false)

                if (page.layoutStyle == StaticLayoutStyle.INFO_CARDS) {
                    val tv = view.findViewById<TextView>(R.id.tvCard2Text)
                    tv.text = HtmlCompat.fromHtml(
                        getString(R.string.onboarding_card2_text),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }

                view
            }

            OnboardingScreenType.OPTION -> {
                val view = inflater.inflate(R.layout.fragment_onboarding_option, container, false)
                renderOption(view)
                view
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCtaEnabledState()
    }

    // ===== OPTION SCREEN RENDER =====
    private fun renderOption(view: View) {
        val titleTextView = view.findViewById<TextView>(R.id.tvTitle)
        val optionContainer = view.findViewById<LinearLayout>(R.id.optionContainer)

        titleTextView.text = page.title
        optionContainer.removeAllViews()

        // OPTION screen: default disable CTA
        setCtaEnabled(false)

        page.options?.forEach { option ->
            val itemView = layoutInflater.inflate(
                R.layout.item_onboarding_option,
                optionContainer,
                false
            )

            // Title
            itemView.findViewById<TextView>(R.id.tvOptionTitle).text = option.title
            val subView = itemView.findViewById<TextView>(R.id.tvOptionSub)
            if (option.subtitle.isNullOrEmpty()) {
                subView?.visibility = View.GONE
            } else {
                subView?.visibility = View.VISIBLE
                subView?.text = option.subtitle
            }

            // Set icon for each option dynamically
            val optionIcon = itemView.findViewById<ImageView>(R.id.imgIcon)
            optionIcon.setImageResource(option.iconResource)  // Gán icon từ OnboardingOption

            itemView.setOnClickListener {
                selectedOptionId = option.id
                selectOnlyOne(optionContainer, itemView)
                setCtaEnabled(true)
            }

            optionContainer.addView(itemView)
        }
    }

    private fun selectOnlyOne(container: LinearLayout, selected: View) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            child.findViewById<ImageView>(R.id.imgRadio)
                .setImageResource(R.drawable.ic_radio_unchecked)
        }
        selected.findViewById<ImageView>(R.id.imgRadio)
            .setImageResource(R.drawable.ic_radio_checked)
    }

    // ===== CTA CONTROL (NO INTERFACE) =====
    private fun updateCtaEnabledState() {
        val shouldEnable = when (page.screenType) {
            OnboardingScreenType.STATIC -> true
            OnboardingScreenType.OPTION -> selectedOptionId != null
        }
        setCtaEnabled(shouldEnable)
    }

    private fun setCtaEnabled(enabled: Boolean) {
        // btnCta nằm trong Activity (activity_onboarding.xml)
        val ctaButton = activity?.findViewById<View>(R.id.btnCta)
        ctaButton?.isEnabled = enabled

        // optional: alpha giống iOS (enabled đậm, disabled mờ)
        ctaButton?.alpha = if (enabled) 1.0f else 0.45f
    }

    companion object {
        private const val ARG_PAGE = "page"

        fun newInstance(page: OnboardingPage): OnboardingFragment {
            return OnboardingFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PAGE, page)
                }
            }
        }
    }
}
