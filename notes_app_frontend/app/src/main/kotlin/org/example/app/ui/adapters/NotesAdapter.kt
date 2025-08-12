package org.example.app.ui.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.example.app.R
import org.example.app.data.Note
import org.example.app.data.NotesRepository
import java.util.*

/**
 * Adapter for displaying notes in a list.
 */
class NotesAdapter(
    private val context: Context,
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Boolean
) : RecyclerView.Adapter<NotesAdapter.NoteVH>() {

    private val items = ArrayList<Note>()
    private var repository: NotesRepository? = null

    fun submit(notes: List<Note>, repo: NotesRepository) {
        items.clear()
        items.addAll(notes)
        repository = repo
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteVH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: NoteVH, position: Int) {
        val note = items[position]
        holder.title.text = note.title
        holder.content.text = note.content?.take(200) ?: ""
        val date = Date(note.updatedAt)
        holder.date.text = DateFormat.getMediumDateFormat(context).format(date) + " " +
                DateFormat.getTimeFormat(context).format(date)
        val catName = repository?.getCategoryNameById(note.categoryId) ?: context.getString(R.string.category_none)
        holder.category.text = catName

        holder.card.setOnClickListener { onClick(note) }
        holder.card.setOnLongClickListener { onLongClick(note) }
    }

    class NoteVH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.card_note)
        val title: TextView = v.findViewById(R.id.text_title)
        val content: TextView = v.findViewById(R.id.text_content)
        val category: TextView = v.findViewById(R.id.text_category)
        val date: TextView = v.findViewById(R.id.text_date)
    }
}
