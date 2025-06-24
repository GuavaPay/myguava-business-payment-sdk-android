package com.guavapay.paymentsdk.platform.threading

import java.util.concurrent.Executor

internal class DirectExecutor : Executor {
  override fun execute(r: Runnable) = r.run()

  companion object {
    internal val unbounded = DirectExecutor()
  }
}