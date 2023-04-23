package ca.on.hojat.renderer.exif

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import timber.log.Timber

object StreamUtils {

    private const val DEFAULT_BUFFER_SIZE = 1024 * 4
    private const val EOF = -1

    @JvmStatic
    @Throws(IOException::class)
    fun copy(
        input: InputStream,
        output: OutputStream
    ): Int {
        val count = copyLarge(input, output)

        if (count > Int.MAX_VALUE)
            return -1

        return count.toInt()
    }

    @JvmStatic
    fun closeQuietly(closeable: Closeable) {
        try {
            closeable.close()
        } catch (ioe: IOException) {
            Timber.e(ioe)
        }
    }

    @Throws(IOException::class)
    private fun copyLarge(input: InputStream, output: OutputStream): Long {
        var count = 0L
        var n = 0
        val buffer = byteArrayOf(DEFAULT_BUFFER_SIZE.toByte())

        while (EOF != input.read(buffer).also { lengthOfInput ->
                n = lengthOfInput
            }) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }

        return count
    }
}