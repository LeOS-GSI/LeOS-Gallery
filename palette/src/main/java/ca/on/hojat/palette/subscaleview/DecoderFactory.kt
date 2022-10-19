package ca.on.hojat.palette.subscaleview

interface DecoderFactory<T> {
    fun make(): T
}
