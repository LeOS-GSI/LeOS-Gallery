package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import ca.on.hojat.palette.views.MyGridLayoutManager
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.adapters.MediaAdapter
import ca.on.sudbury.hojat.smartgallery.asynctasks.GetMediaAsynctask
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentMediumPickerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getCachedMedia
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.GridSpacingItemDecoration
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.ViewType
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailSection

class PickMediumDialogFragment(
    val path: String,
    val callback: (path: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentMediumPickerBinding? = null
    private val binding get() = _binding!!

    // the configuration needed throughout this class
    private var shownMedia = ArrayList<ThumbnailItem>()
    private lateinit var config: Config
    private var isGridViewType = false


    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentMediumPickerBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {
        // need to load the config before drawing UI
        config = requireActivity().config
        val viewType = config.getFolderViewType(if (config.showAll) SHOW_ALL else path)
        isGridViewType = viewType == ViewType.Grid.id

        (binding.mediaGrid.layoutManager as MyGridLayoutManager).apply {
            orientation =
                if (config.scrollHorizontally && isGridViewType) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
            spanCount = if (isGridViewType) config.mediaColumnCnt else 1
        }
        binding.mediaFastscroller.updateColors(requireActivity().getProperPrimaryColor())
        requireActivity().getCachedMedia(path) {
            val media = it.filter { it is Medium } as ArrayList
            if (media.isNotEmpty()) {
                requireActivity().runOnUiThread {
                    gotMedia(media)
                }
            }
        }
        GetMediaAsynctask(
            requireActivity(),
            path,
            isPickImage = false,
            isPickVideo = false,
            showAll = false
        ) {
            gotMedia(it)
        }.execute()

    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnPositiveMediumPicker.setOnClickListener { dismiss() }
            btnNegativeMediumPicker.setOnClickListener { dismiss() }
            btnNeutralMediumPicker.setOnClickListener {
                showOtherFolder()
                dismiss()
            }
        }
    }

    private fun showOtherFolder() {
        PickDirectoryDialog(
            requireActivity() as BaseSimpleActivity,
            path,
            showOtherFolderButton = true,
            showFavoritesBin = true,
            isPickingCopyMoveDestination = false,
            isPickingFolderForWidget = false
        ) {
            callback(it)
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun gotMedia(media: ArrayList<ThumbnailItem>) {
        if (media.hashCode() == shownMedia.hashCode())
            return

        shownMedia = media
        val adapter = MediaAdapter(
            requireActivity() as BaseSimpleActivity,
            shownMedia.clone() as ArrayList<ThumbnailItem>,
            listener = null,
            isAGetIntent = true,
            allowMultiplePicks = false,
            path,
            binding.mediaGrid
        ) {
            if (it is Medium) {
                callback(it.path)
                dismiss()
            }
        }

        val scrollHorizontally = config.scrollHorizontally && isGridViewType
        binding.apply {
            mediaGrid.adapter = adapter
            mediaFastscroller.setScrollVertically(!scrollHorizontally)
        }
        handleGridSpacing(media)
    }

    private fun handleGridSpacing(media: ArrayList<ThumbnailItem>) {
        if (isGridViewType) {
            val spanCount = config.mediaColumnCnt
            val spacing = config.thumbnailSpacing
            val useGridPosition = media.firstOrNull() is ThumbnailSection

            var currentGridDecoration: GridSpacingItemDecoration? = null
            if (binding.mediaGrid.itemDecorationCount > 0) {
                currentGridDecoration =
                    binding.mediaGrid.getItemDecorationAt(0) as GridSpacingItemDecoration
                currentGridDecoration.items = media
            }

            val newGridDecoration = GridSpacingItemDecoration(
                spanCount,
                spacing,
                config.scrollHorizontally,
                config.fileRoundedCorners,
                media,
                useGridPosition
            )
            if (currentGridDecoration.toString() != newGridDecoration.toString()) {
                if (currentGridDecoration != null) {
                    binding.mediaGrid.removeItemDecoration(currentGridDecoration)
                }
                binding.mediaGrid.addItemDecoration(newGridDecoration)
            }
        }
    }

}