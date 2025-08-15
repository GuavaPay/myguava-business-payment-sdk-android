package com.guavapay.paymentsdk.presentation.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import java.io.Serializable
import java.util.UUID

internal class NavigationEvents {
  private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
  private val events = _events.asSharedFlow()

  suspend fun fire(event: Event) = _events.emit(event)
  suspend inline fun <reified T : Event> await(key: String) = events.filter { it.key == key }.filterIsInstance<T>().first()

  sealed class Event(open val key: String) : Serializable {
    data class ConfirmCardEdit(override val key: String, val cardName: String) : Event(key)
    data class ConfirmCardRemove(override val key: String) : Event(key)
    data class ContactResult(override val key: String, val email: String?, val phone: String?) : Event(key)
    data class PhoneResult(override val key: String, val countryCode: String, val countryIso: String) : Event(key)
  }

  companion object {
    fun key() = UUID.randomUUID().toString()
  }
}
