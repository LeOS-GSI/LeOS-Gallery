package ca.on.sudbury.hojat.smartgallery.photoview

import android.os.Bundle
import ca.on.sudbury.hojat.smartgallery.base.PhotoVideoActivity

class PhotoActivity : PhotoVideoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = false
        super.onCreate(savedInstanceState)
    }
}
