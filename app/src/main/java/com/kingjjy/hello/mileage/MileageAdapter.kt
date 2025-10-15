package com.kingjjy.hello.mileage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MileageAdapter(
    private val context: Context,
    private val items: List<MileageItem>,
    private val onToggle: (MileageItem, Boolean) -> Unit,
    private val isDone: (MileageItem) -> Boolean
) : RecyclerView.Adapter<MileageAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val check: CheckBox = v.findViewById(R.id.check)
        val title: TextView = v.findViewById(R.id.title)
        val open: Button = v.findViewById(R.id.open)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_mileage, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = isDone(item)
        holder.check.setOnCheckedChangeListener { _, checked ->
            onToggle(item, checked)
        }
        holder.open.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int = items.size
}
