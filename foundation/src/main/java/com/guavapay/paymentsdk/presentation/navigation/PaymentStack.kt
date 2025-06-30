package com.guavapay.paymentsdk.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

@Composable internal fun <T : Serializable> rememberNavBackStack(vararg elements: T) =
  rememberSaveable(saver = snapshotStateListSaver(javaSerializableListSaver())) {
    elements.toList().toMutableStateList()
  }

internal typealias NavBackStack = SnapshotStateList<Serializable>

private fun <T : Serializable> javaSerializableListSaver() =
  Saver<List<T>, ByteArray>(
    save = { list ->
      ByteArrayOutputStream().use { bos ->
        ObjectOutputStream(bos).use { oos ->
          oos.writeObject(ArrayList(list))
        }
        bos.toByteArray()
      }
    },
    restore = { bytes ->
      ByteArrayInputStream(bytes).use { bis ->
        ObjectInputStream(bis).use { ois ->
          (ois.readObject() as ArrayList<T>)
        }
      }
    }
  )

private fun <T> snapshotStateListSaver(listSaver: Saver<List<T>, out Any>): Saver<SnapshotStateList<T>, Any> =
  with(listSaver as Saver<List<T>, Any>) {
    Saver(
      save = { state -> save(state.toList().toMutableList()) },
      restore = { state -> restore(state)?.toMutableStateList() },
    )
  }