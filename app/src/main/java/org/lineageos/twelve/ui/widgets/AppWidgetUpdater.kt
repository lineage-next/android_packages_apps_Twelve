/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import kotlin.reflect.KClass

/**
 * A helper class used to update a widget.
 */
abstract class AppWidgetUpdater<T : AppWidgetProvider>(
    private val appWidgetProviderKClass: KClass<T>,
    @LayoutRes val layoutResId: Int,
) {
    abstract suspend fun RemoteViews.update(context: Context)

    suspend fun update(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            // Get the layout for the widget
            val views = RemoteViews(context.packageName, layoutResId).apply {
                update(context)
            }

            // Tell the AppWidgetManager to perform an update on the current widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    suspend fun update(context: Context) = AppWidgetManager.getInstance(context).let {
        update(
            context,
            it,
            it.getAppWidgetIds(
                ComponentName(context, appWidgetProviderKClass.java)
            ),
        )
    }
}
