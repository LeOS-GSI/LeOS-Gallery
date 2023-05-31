package ca.on.sudbury.hojat.smartgallery.interfaces


import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity

interface RenameTab {
    fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>)

    fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit)
}
