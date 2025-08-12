package org.example.app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.Category
import org.example.app.data.Note
import org.example.app.data.NotesRepository
import org.example.app.ui.adapters.NotesAdapter

/**
 * HomeFragment lists notes and lets users open them in the detail drawer.
 */
class HomeFragment : Fragment() {

    interface NoteSelectionListener {
        fun onNoteSelected(note: Note)
    }

    private var noteSelectionListener: NoteSelectionListener? = null
    private lateinit var repository: NotesRepository

    private lateinit var recycler: RecyclerView
    private lateinit var emptyLabel: TextView

    private var filterCategory: Category? = null
    private var adapter: NotesAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NoteSelectionListener) {
            noteSelectionListener = context
        }
        repository = NotesRepository(context)
    }

    override fun onDetach() {
        super.onDetach()
        noteSelectionListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recycler = view.findViewById(R.id.recycler_notes)
        emptyLabel = view.findViewById(R.id.text_empty)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotesAdapter(
            requireContext(),
            onClick = { note -> noteSelectionListener?.onNoteSelected(note) },
            onLongClick = { note -> noteSelectionListener?.onNoteSelected(note); true }
        )
        recycler.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // PUBLIC_INTERFACE
    fun refresh() {
        /** Reloads notes, applying category filter if set. */
        val notes = if (filterCategory == null) {
            repository.getAllNotes()
        } else {
            repository.getAllNotes(filterCategory!!.id)
        }
        adapter?.submit(notes, repository)
        emptyLabel.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
    }

    // PUBLIC_INTERFACE
    fun setFilterCategory(category: Category?) {
        /** Sets the category filter for the list. Pass null to clear filter. */
        filterCategory = category
        refresh()
    }
}
