package com.example.apptest1.model

import java.io.Serializable

enum class OnboardingScreenType {
    STATIC,
    OPTION
}

enum class StaticLayoutStyle {
    NORMAL,
    BIG_COUNTER_44,
    INFO_CARDS,
    EXAMPLE_SCAM,
    REAL_PROTECTION
}
data class InfoCard(
    val highlight: String? = null, // "73%", "1,275%"
    val text: String
)

data class BulletItem(
    val text: String
)
data class OnboardingOption(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconResource: Int
)
data class OnboardingPage(
    val index: Int,
    val screenType: OnboardingScreenType,

    val title: String,
    val description: String? = null,
    val ctaText: String,

    // STATIC
    val layoutStyle: StaticLayoutStyle = StaticLayoutStyle.NORMAL,

    // OPTION (để sau)
    val options: List<OnboardingOption>? = null
) : Serializable
