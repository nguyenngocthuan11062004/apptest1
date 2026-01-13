package com.example.apptest1.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnboardingPage(
    val index: Int,
    val title: String,
    val description: String,
    val ctaText: String
) : Parcelable
