package com.guavapay.paymentsdk.platform.threading

import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun <T> Task<T>.await(cts: CancellationTokenSource? = null): Task<T> {
  return if (isComplete) this else suspendCancellableCoroutine { cont ->
    addOnCompleteListener(DirectExecutor.unbounded, cont::resume)
    cts?.let { cancellationSource -> cont.invokeOnCancellation { cancellationSource.cancel() } }
  }
}