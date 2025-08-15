package com.guavapay.paymentsdk.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import java.io.Serializable

internal class NavigationEvents {
  private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
  val events = _events.asSharedFlow()

  suspend fun fire(event: Event) = _events.emit(event)
  suspend inline fun <reified T : Event> await() = events.filterIsInstance<T>().first()

  sealed class Event : Serializable {
    data class ConfirmCardEdit(val cardName: String) : Event()
    data object ConfirmCardRemove : Event() { private fun readResolve(): Any = ConfirmCardRemove }
    data class ContactResult(val email: String?, val phone: String?) : Event()
    data class PhoneResult(val countryCode: String, val countryIso: String) : Event()
  }
}
