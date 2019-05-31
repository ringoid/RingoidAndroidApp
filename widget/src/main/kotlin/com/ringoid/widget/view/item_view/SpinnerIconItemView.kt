package com.ringoid.widget.view.item_view

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.ringoid.widget.R
import com.ringoid.widget.model.IListItem
import kotlinx.android.synthetic.main.widget_spinner_icon_item_view_layout.view.*

class SpinnerIconItemView : IconItemView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    override fun getLayoutId(): Int = R.layout.widget_spinner_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    fun <T : IListItem> setItems(items: List<T>) {
        spinner.adapter = ItemAdapter(items)
    }

    // --------------------------------------------------------------------------------------------
    private inner class ItemViewHolder {
        lateinit var tvLabel: TextView
    }

    private inner class ItemAdapter<T : IListItem>(private val items: List<T>) : SpinnerAdapter {

        override fun hasStableIds(): Boolean = true

        override fun isEmpty(): Boolean = items.isEmpty()

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Any = items[position]

        override fun getItemId(position: Int): Long = items[position].id.toLong()

        override fun getItemViewType(position: Int): Int = 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var xconvertView = convertView
            val holder = convertView
                ?.let { it.tag as ItemViewHolder }
                ?: run {
                    xconvertView = LayoutInflater.from(parent.context).inflate(R.layout.widget_spinner_list_item, parent, false)
                    val viewHolder = ItemViewHolder().apply {
                        tvLabel = xconvertView!!.findViewById(R.id.tv_label)
                    }
                    xconvertView!!.tag = viewHolder
                    viewHolder
                }
            holder.tvLabel.setText(items[position].getLabelResId())
            return xconvertView!!
        }

        override fun getViewTypeCount(): Int = 1

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
            getView(position, convertView, parent)

        override fun registerDataSetObserver(observer: DataSetObserver) {}
        override fun unregisterDataSetObserver(observer: DataSetObserver) {}
    }
}
