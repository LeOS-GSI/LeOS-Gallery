package com.simplemobiletools.gallery.pro.activities

import android.os.Bundle
import com.simplemobiletools.gallery.pro.base.PhotoVideoActivity

class VideoActivity : PhotoVideoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = true
        super.onCreate(savedInstanceState)
    }
}
