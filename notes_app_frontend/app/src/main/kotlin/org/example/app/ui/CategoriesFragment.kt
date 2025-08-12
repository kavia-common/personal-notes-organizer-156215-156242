package org.example.app.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.data.Category
import org.example.app.data.NotesRepository
import org.example.app.ui.adapters.CategoriesAdapter

/**
 * CategoriesFragment shows categories and allows adding/removing/renaming.
 */
class CategoriesFragment : Fragment() {

    interface CategorySelectionListener {
        fun onCategorySelected(category: Category)
        fun onAddCategoryRequested()
    }

    private var listener: CategorySelectionListener? = null
    private lateinit var repository: NotesRepository

    private lateinit var recycler: RecyclerView
    private lateinit var emptyLabel: TextView
    private lateinit var addButton: Button
    private var adapter: CategoriesAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CategorySelectionListener) {
            listener = context
        }
        repository = NotesRepository(context)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)
        recycler = view.findViewById(R.id.recycler_categories)
        emptyLabel = view.findViewById(R.id.text_empty_categories)
        addButton = view.findViewById(R.id.btn_add_category)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = CategoriesAdapter(
            onClick = { category -> listener?.onCategorySelected(category) },
            onLongClick = { category ->
                showCategoryOptionsDialog(category)
                true
            }
        )
        recycler.adapter = adapter

        addButton.setOnClickListener { promptAddCategory() }

        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // PUBLIC_INTERFACE
    fun refresh() {
        /** Reload categories. */
        val cats = repository.getAllCategories()
        adapter?.submit(cats)
        emptyLabel.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
    }

    // PUBLIC_INTERFACE
    fun promptAddCategory() {
        /** Prompts user to add a new category. */
        val input = EditText(requireContext())
        input.hint = getString(R.string.hint_category_name)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_new_category)
            .setView(input)
            .setPositiveButton(R.string.action_add) { d, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isNotEmpty()) {
                    repository.insertCategory(name)
                    refresh()
                }
                d.dismiss()
            }
            .setNegativeButton(R.string.action_cancel) { d, _ -> d.dismiss() }
            .show()
    }

    private fun showCategoryOptionsDialog(category: Category) {
        val options = arrayOf(
            getString(R.string.action_rename),
            getString(R.string.action_delete)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(category.name)
            .setItems(options) { d, which ->
                when (which) {
                    0 -> promptRenameCategory(category)
                    1 -> {
                        repository.deleteCategory(category.id)
                        refresh()
                    }
                }
                d.dismiss()
            }
            .show()
    }

    private fun promptRenameCategory(category: Category) {
        val input = EditText(requireContext())
        input.setText(category.name)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_rename_category)
            .setView(input)
            .setPositiveButton(R.string.action_rename) { d, _ ->
                val newName = input.text?.toString()?.trim().orEmpty()
                if (newName.isNotEmpty()) {
                    repository.renameCategory(category.id, newName)
                    refresh()
                }
                d.dismiss()
            }
            .setNegativeButton(R.string.action_cancel) { d, _ -> d.dismiss() }
            .show()
    }
}
