/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import androidx.annotation.IntRange

typealias UserRating = @receiver:IntRange(from = 1, to = 5) Int
