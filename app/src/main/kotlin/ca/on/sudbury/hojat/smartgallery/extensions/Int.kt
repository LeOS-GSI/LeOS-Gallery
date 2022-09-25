package ca.on.sudbury.hojat.smartgallery.extensions

import com.simplemobiletools.commons.helpers.SORT_DESCENDING

fun Int.isSortingAscending() = this and SORT_DESCENDING == 0
