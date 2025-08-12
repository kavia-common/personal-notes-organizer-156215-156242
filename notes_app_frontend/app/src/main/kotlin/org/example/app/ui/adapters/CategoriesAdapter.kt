package org.example.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.example.app.R
import org.example.app.data.Category

/**
 * Adapter for categories list.
 */
class CategoriesAdapter(
    private val onClick: (Category) -> Unit,
    private val onLongClick: (Category) -> Boolean
) : RecyclerView.Adapter<CategoriesAdapter.CatVH>() {

    private val items = ArrayList<Category>()

    fun submit(categories: List<Category>) {
        items.clear()
        items.addAll(categories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CatVH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CatVH, position: Int) {
        val category = items[position]
        holder.name.text = category.name
        holder.card.setOnClickListener { onClick(category) }
        holder.card.setOnLongClickListener { onLongClick(category) }
    }

    class CatVH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.card_category)
        val name: TextView = v.findViewById(R.id.text_category_name)
    }
}
