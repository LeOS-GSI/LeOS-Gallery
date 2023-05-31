package ca.on.sudbury.hojat.smartgallery.models


import java.io.Serializable

data class FaqItem(val title: Any, val text: Any) : Serializable {
    companion object {
        private const val serialVersionUID = -6553345863512345L
    }
}
