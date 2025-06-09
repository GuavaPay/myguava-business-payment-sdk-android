package com.guavapay.paymentsdk.platform.threading

import com.guavapay.paymentsdk.logging.d
import java.util.concurrent.ThreadFactory

private typealias JvmThreadFactory = ThreadFactory

private inline fun report(thread: Thread) = d("Thread spawned for pool ${thread.name.substringBefore("-")}, thread: ${thread.name}")
private inline fun thread(prefix: String, task: Runnable) = Thread(task).apply { name = "$prefix-thread-$id" }

internal class ThreadFactory(prefix: String) : JvmThreadFactory by (JvmThreadFactory { task -> thread(prefix, task).also(::report) })
