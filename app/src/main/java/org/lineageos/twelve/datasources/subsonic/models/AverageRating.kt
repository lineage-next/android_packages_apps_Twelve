/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import androidx.annotation.FloatRange

typealias AverageRating = @receiver:FloatRange(from = 0.0, to = 5.0) Double
