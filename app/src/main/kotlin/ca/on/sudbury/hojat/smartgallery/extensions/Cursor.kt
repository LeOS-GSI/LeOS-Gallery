package ca.on.sudbury.hojat.smartgallery.extensions

import android.annotation.SuppressLint
import android.database.Cursor

@SuppressLint("Range")
fun Cursor.getLongValue(key: String) = getLong(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))
