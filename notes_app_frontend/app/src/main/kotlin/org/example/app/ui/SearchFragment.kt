package org.example.app.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.Note
import org.example.app.data.NotesRepository
import org.example.app.ui.adapters.NotesAdapter

/**
 * SearchFragment allows searching notes by title/content.
 */
class SearchFragment : Fragment() {

    interface NoteSelectionListener {
        fun onNoteSelected(note: Note)
    }

    private var noteSelectionListener: NoteSelectionListener? = null
    private lateinit var repository: NotesRepository

    private lateinit var searchInput: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var emptyLabel: TextView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchInput = view.findViewById(R.id.input_search)
        recycler = view.findViewById(R.id.recycler_search)
        emptyLabel = view.findViewById(R.id.text_empty_search)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotesAdapter(
            requireContext(),
            onClick = { note -> noteSelectionListener?.onNoteSelected(note) },
            onLongClick = { note -> noteSelectionListener?.onNoteSelected(note); true }
        )
        recycler.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s?.toString().orEmpty())
            }
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // PUBLIC_INTERFACE
    fun refresh() {
        /** Refreshes the list using the current query. */
        performSearch(searchInput.text?.toString().orEmpty())
    }

    private fun performSearch(query: String) {
        val q = query.trim()
        val results = if (q.isEmpty()) emptyList() else repository.searchNotes(q)
        adapter?.submit(results, repository)
        emptyLabel.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }
}
