package com.guavapay.paymentsdk.presentation.platform

import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

@ReadOnlyComposable @Composable internal fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window