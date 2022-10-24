package ca.on.sudbury.hojat.smartgallery.extensions

fun Any.toStringSet() = toString().split(",".toRegex()).toSet()
