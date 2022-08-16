package com.simplemobiletools.gallery.pro.photoview

import android.os.Bundle
import com.simplemobiletools.gallery.pro.base.PhotoVideoActivity

class PhotoActivity : PhotoVideoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = false
        super.onCreate(savedInstanceState)
    }
}
