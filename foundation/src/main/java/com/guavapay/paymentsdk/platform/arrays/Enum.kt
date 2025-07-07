package com.guavapay.paymentsdk.platform.arrays

@JvmName("intersectByName0") inline fun <reified T : Enum<T>> Collection<T>.intersectByName(other: Collection<String>): Set<T> {
  val thisNames = this.map { it.name }.toSet()
  val intersection = thisNames.intersect(other.toSet())
  return intersection.mapNotNull { name -> enumValues<T>().find { it.name == name } }.toSet()
}

inline fun <reified T : Enum<T>> Collection<String>.intersectByName(enumClass: Collection<T>) = enumClass.intersectByName(this)
inline fun <reified T : Enum<T>, reified R : Enum<R>> Collection<T>.intersectByNameWith(other: Collection<R>) = intersectByName(other.map(Enum<R>::name).toSet())
