package ca.on.sudbury.hojat.smartgallery.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.models.FilterItem
import kotlinx.android.synthetic.main.editor_filter_item.view.*


class FiltersAdapter(
    val context: Context,
    private val filterItems: ArrayList<FilterItem>,
    private val itemClick: (Int) -> Unit
) :
    RecyclerView.Adapter<FiltersAdapter.ViewHolder>() {

    private var currentSelection = filterItems.first()

    @SuppressLint("UseCompatLoadingForDrawables")
    private var strokeBackground = context.resources.getDrawable(R.drawable.stroke_background)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(filterItems[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.editor_filter_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = filterItems.size

    fun getCurrentFilter() = currentSelection

    @SuppressLint("NotifyDataSetChanged")
    private fun setCurrentFilter(position: Int) {
        val filterItem = filterItems.getOrNull(position) ?: return
        if (currentSelection != filterItem) {
            currentSelection = filterItem
            notifyDataSetChanged()
            itemClick.invoke(position)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(filterItem: FilterItem): View {
            itemView.apply {
                editor_filter_item_label.text = filterItem.filter.name
                editor_filter_item_thumbnail.setImageBitmap(filterItem.bitmap)
                editor_filter_item_thumbnail.background = if (getCurrentFilter() == filterItem) {
                    strokeBackground
                } else {
                    null
                }

                setOnClickListener {
                    setCurrentFilter(adapterPosition)
                }
            }
            return itemView
        }
    }
}
