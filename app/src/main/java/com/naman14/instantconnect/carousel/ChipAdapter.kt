package com.naman14.instantconnect.carousel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.naman14.instantconnect.R

public class ChipAdapter :
    RecyclerView.Adapter<ChipAdapter.MyViewHolder>() {

    private var items: List<String> = arrayListOf()

    private val checkedChipId: MutableSet<Int> = HashSet(
        itemCount
    )

    /** Provide a reference to the views for each data item.  */
    public class MyViewHolder(view: View, checkedChipId: MutableSet<Int>) :
        RecyclerView.ViewHolder(view) {
        var chip: Chip

        init {
            chip = view as Chip
            chip.setOnClickListener { v: View ->
                val chipId = v.tag as Int
                if (chip.isChecked) {
                    checkedChipId.add(chipId)
                } else {
                    checkedChipId.remove(chipId)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.cat_chip_group_item_filter, parent, false)
        return MyViewHolder(view, checkedChipId)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.chip.tag = position
        holder.chip.isChecked = false
        holder.chip.text = items.get(position)
    }

    fun setData(items: List<String>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

