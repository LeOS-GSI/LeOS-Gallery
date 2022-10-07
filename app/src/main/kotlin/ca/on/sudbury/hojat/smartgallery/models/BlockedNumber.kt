package ca.on.sudbury.hojat.smartgallery.models

data class BlockedNumber(
    val id: Long,
    val number: String,
    val normalizedNumber: String,
    val numberToCompare: String
)
