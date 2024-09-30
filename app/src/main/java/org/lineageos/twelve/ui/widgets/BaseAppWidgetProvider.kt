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
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.MainScope

/**
 * Base class for all of the app's app widget providers.
 * Implement [RemoteViews.update] to update the [RemoteViews] with the latest data you have.
 * Then you can call [update] to update the widget when you need to.
 * The system may also want to update the widget, so be sure to handle that as well.
 *
 * @param layoutResId The layout resource ID for the widget
 */
abstract class BaseAppWidgetProvider(
    @LayoutRes private val layoutResId: Int,
) : AppWidgetProvider(), LifecycleOwner {
    private val dispatcher = AppWidgetProviderLifecycleDispatcher(this)
    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    protected val coroutineScope = MainScope()

    protected var context: Context? = null
        private set

    /**
     * Called when someone (either you or the system) wants to update the widget.
     */
    abstract fun RemoteViews.update(context: Context)

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        context ?: return
        appWidgetManager ?: return
        appWidgetIds ?: return

        update(context, appWidgetManager, *appWidgetIds)
    }

    @CallSuper
    override fun onEnabled(context: Context?) {
        this.context = context!!
        dispatcher.onProviderPreSuperOnEnabled()
        super.onEnabled(context)
    }

    @CallSuper
    override fun onDisabled(context: Context?) {
        dispatcher.onProviderPreSuperOnDisabled()
        this.context = null
        super.onDisabled(context)
    }

    /**
     * Called when someone (either you or the system) wants to update the widget.
     */
    protected fun update(
        context: Context,
        appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        vararg appWidgetIds: Int
    ) {
        val ids = appWidgetIds.takeIf { it.isNotEmpty() } ?: appWidgetManager.getAppWidgetIds(
            ComponentName(context, javaClass)
        )

        ids.forEach { appWidgetId ->
            // Get the layout for the widget
            val views = RemoteViews(context.packageName, layoutResId).apply {
                update(context)
            }

            // Tell the AppWidgetManager to perform an update on the current widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
