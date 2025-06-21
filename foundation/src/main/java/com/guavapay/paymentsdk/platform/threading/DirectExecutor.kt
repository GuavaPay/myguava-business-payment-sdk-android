package com.guavapay.paymentsdk.platform.threading

import java.util.concurrent.Executor

internal class DirectExecutor : Executor { // TODO: Move to executors in CoroutineUnit without companion obj.
  override fun execute(r: Runnable) = r.run()

  companion object {
    internal val unbounded = DirectExecutor()
  }
}