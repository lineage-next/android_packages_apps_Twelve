/*
 * SPDX-FileCopyrightText: 2022-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import androidx.core.view.isVisible

fun View.slide() {
    if (isVisible) {
        slideDown()
    } else {
        slideUp()
    }
}

fun View.slideUp() {
    if (isVisible) {
        return
    }

    isVisible = true

    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

    startAnimation(
        AnimationSet(true).apply {
            addAnimation(
                TranslateAnimation(
                    0f, 0f, measuredHeight.toFloat(), 0f
                ).apply {
                    duration = 250
                }
            )
            addAnimation(
                AlphaAnimation(0.0f, 1.0f).apply {
                    duration = 250
                }
            )
        }
    )
}

fun View.slideDown() {
    if (!isVisible) {
        return
    }

    isVisible = false

    startAnimation(
        AnimationSet(true).apply {
            addAnimation(
                TranslateAnimation(0f, 0f, 0f, height.toFloat()).apply {
                    duration = 200
                }
            )
            addAnimation(
                AlphaAnimation(1.0f, 0.0f).apply {
                    duration = 200
                }
            )
        }
    )
}
