package com.ringoid.widget.view.item_view

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.ringoid.utility.getAttributeColor
import com.ringoid.widget.R
import com.ringoid.widget.model.IListItem
import kotlinx.android.synthetic.main.widget_spinner_icon_item_view_layout.view.*

class SpinnerIconItemView : IconItemView {

    companion object {
        private var textColorPrimary: Int = -1
        private var textColorSecondary: Int = -1
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        if (textColorPrimary == -1) textColorPrimary = context.getAttributeColor(R.attr.refTextColorPrimary)
        if (textColorSecondary == -1) textColorSecondary = context.getAttributeColor(R.attr.refTextColorSecondary)
    }

    override fun getLayoutId(): Int = R.layout.widget_spinner_icon_item_view_layout

    /* API */
    // --------------------------------------------------------------------------------------------
    fun <T : IListItem> setItems(items: List<T>) {
        spinner.adapter = ItemAdapter(items)
    }

    @Suppress("unchecked_cast")
    fun <T : IListItem> setSelectedItem(item: T) {
        (spinner.adapter as ItemAdapter<T>).getItemPosition { it.id == item.id }
            .takeIf { it != -1 }
            ?.let { position ->
                val listener = spinner.onItemSelectedListener
                spinner.onItemSelectedListener = null
                spinner.setSelection(position)  // set selected item w/o calling listener
                spinner.onItemSelectedListener = listener
            }
    }

    /* Listener */
    // ------------------------------------------
    @Suppress("unchecked_cast")
    fun <T : IListItem> setOnItemSelectedListener(l: ((item: T) -> Unit)?) {
        if (l == null) {
            spinner.onItemSelectedListener = null
            return
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (spinner.adapter as ItemAdapter<T>).getItemById(id)
                    ?.let { it as? T }
                    ?.let { item -> l.invoke(item) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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

        internal fun getItemById(id: Long): Any? = items.find { it.id == id.toInt() }

        internal fun getItemPosition(predicate: (item: T) -> Boolean): Int = items.indexOfFirst(predicate)

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
            items[position].let {
                with(holder.tvLabel) {
                    val color = if (it.isDefault) textColorSecondary else textColorPrimary
                    setText(it.getLabelResId ())
                    setTextColor(color)
                }
            }
            return xconvertView!!
        }

        override fun getViewTypeCount(): Int = 1

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
            getView(position, convertView, parent)

        override fun registerDataSetObserver(observer: DataSetObserver) {}
        override fun unregisterDataSetObserver(observer: DataSetObserver) {}
    }
}
