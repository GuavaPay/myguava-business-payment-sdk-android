package com.guavapay.paymentsdk.platform.reflection

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE
import java.lang.StackWalker.Option.SHOW_REFLECT_FRAMES
import java.lang.StackWalker.StackFrame

internal object CallerFactory {
  context(_: Types.Class) fun caller(offset: Int): Class<*> {
    return if (SDK_INT >= UPSIDE_DOWN_CAKE) {
      StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk { it.map(StackFrame::getDeclaringClass).skip(offset + 1L /* this */).findFirst().get() }
    } else {
      Throwable().stackTrace.asSequence().map(StackTraceElement::getClassName).map { Class.forName(it) }.drop(offset + 1 /* this */).first()
    }
  }

  context(_: Types.Method) fun caller(offset: Int): String /* Method name */ {
    return if (SDK_INT >= UPSIDE_DOWN_CAKE) {
      StackWalker.getInstance(SHOW_REFLECT_FRAMES).walk { it.map(StackFrame::getMethodName).skip(offset + 1L /* this */).findFirst().get() }
    } else {
      Throwable().stackTrace.asSequence().map(StackTraceElement::getMethodName).drop(offset + 1 /* this */).first()
    }
  }

  sealed interface Types { object Class : Types ; object Method : Types }
}
