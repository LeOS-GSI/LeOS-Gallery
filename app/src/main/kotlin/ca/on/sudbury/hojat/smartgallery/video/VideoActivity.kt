package ca.on.sudbury.hojat.smartgallery.video

import android.os.Bundle
import ca.on.sudbury.hojat.smartgallery.base.PhotoVideoActivity

class VideoActivity : PhotoVideoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = true
        super.onCreate(savedInstanceState)
    }
}
