package org.example.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.example.app.data.Category
import org.example.app.data.Note
import org.example.app.data.NotesRepository
import org.example.app.ui.CategoriesFragment
import org.example.app.ui.HomeFragment
import org.example.app.ui.SearchFragment

/**
 * PUBLIC_INTERFACE
 * MainActivity for the Notes app.
 *
 * This activity hosts a bottom navigation with Home, Search, and Categories screens.
 * It also provides a right-side drawer to view/create/edit notes and a FAB to add a new note.
 *
 * Navigation:
 * - Home: Lists all notes, optionally filtered by category.
 * - Search: Search notes by title/content.
 * - Categories: Manage categories and filter notes by category.
 *
 * The note detail drawer supports create/edit/delete and share actions.
 */
class MainActivity : AppCompatActivity(),
    HomeFragment.NoteSelectionListener,
    SearchFragment.NoteSelectionListener,
    CategoriesFragment.CategorySelectionListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: Toolbar

    // Drawer inputs
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var shareButton: Button

    private lateinit var repository: NotesRepository

    private var currentEditingNote: Note? = null
    private var cachedCategories: List<Category> = emptyList()

    private val homeFragment by lazy { HomeFragment() }
    private val searchFragment by lazy { SearchFragment() }
    private val categoriesFragment by lazy { CategoriesFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = NotesRepository(this)

        // Setup UI
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        bottomNav = findViewById(R.id.bottom_nav)
        fab = findViewById(R.id.fab_add)

        titleInput = findViewById(R.id.input_title)
        contentInput = findViewById(R.id.input_content)
        categorySpinner = findViewById(R.id.spinner_category)
        saveButton = findViewById(R.id.btn_save)
        deleteButton = findViewById(R.id.btn_delete)
        cancelButton = findViewById(R.id.btn_cancel)
        shareButton = findViewById(R.id.btn_share)

        setupBottomNavigation()
        setupFab()
        setupDrawerButtons()

        // Default fragment
        if (savedInstanceState == null) {
            switchFragment(R.id.menu_home)
        }

        refreshCategoriesIntoSpinner()
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            switchFragment(item.itemId)
            true
        }
    }

    private fun setupFab() {
        fab.setOnClickListener {
            // Create mode
            openNoteDrawer(null)
        }
    }

    private fun setupDrawerButtons() {
        saveButton.setOnClickListener { saveNoteFromDrawer() }
        deleteButton.setOnClickListener { deleteCurrentNote() }
        cancelButton.setOnClickListener { closeDrawer() }
        shareButton.setOnClickListener { shareCurrentNote() }
    }

    private fun switchFragment(@IdRes itemId: Int) {
        val fragment = when (itemId) {
            R.id.menu_home -> homeFragment
            R.id.menu_search -> searchFragment
            R.id.menu_categories -> categoriesFragment
            else -> homeFragment
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, fragment::class.java.simpleName)
            .commit()
        bottomNav.menu.findItem(itemId)?.isChecked = true
    }

    private fun refreshCategoriesIntoSpinner() {
        cachedCategories = repository.getAllCategories().sortedBy { it.name.lowercase() }
        val names = mutableListOf<String>()
        names.add(getString(R.string.category_none))
        names.addAll(cachedCategories.map { it.name })
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun resetDrawerFor(note: Note?) {
        currentEditingNote = note
        if (note == null) {
            titleInput.setText("")
            contentInput.setText("")
            categorySpinner.setSelection(0)
            deleteButton.visibility = View.GONE
            shareButton.visibility = View.GONE
            toolbar.subtitle = getString(R.string.drawer_title_new)
        } else {
            titleInput.setText(note.title)
            contentInput.setText(note.content ?: "")
            val pos = if (note.categoryId == null) 0 else {
                val idx = cachedCategories.indexOfFirst { it.id == note.categoryId }
                if (idx >= 0) idx + 1 else 0
            }
            categorySpinner.setSelection(pos)
            deleteButton.visibility = View.VISIBLE
            shareButton.visibility = View.VISIBLE
            toolbar.subtitle = getString(R.string.drawer_title_edit)
        }
    }

    private fun ensureCategoriesSeeded() {
        if (repository.getAllCategories().isEmpty()) {
            repository.insertCategory(getString(R.string.category_general))
        }
    }

    // PUBLIC_INTERFACE
    fun openNoteDrawer(note: Note?) {
        /** Open the right-side drawer to create or edit a note. */
        ensureCategoriesSeeded()
        refreshCategoriesIntoSpinner()
        resetDrawerFor(note)
        drawerLayout.openDrawer(GravityCompat.END)
        // focus title
        titleInput.requestFocus()
        showKeyboard(titleInput)
    }

    private fun closeDrawer() {
        hideKeyboard()
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun saveNoteFromDrawer() {
        val title = titleInput.text?.toString()?.trim().orEmpty()
        val content = contentInput.text?.toString()?.trim()
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.error_title_required, Toast.LENGTH_SHORT).show()
            return
        }
        val selectedPos = categorySpinner.selectedItemPosition
        val categoryId = if (selectedPos <= 0) null else cachedCategories[selectedPos - 1].id

        val now = System.currentTimeMillis()
        val note = currentEditingNote?.copy(
            title = title,
            content = content,
            categoryId = categoryId,
            updatedAt = now
        ) ?: Note(
            id = null,
            title = title,
            content = content,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )

        if (note.id == null) {
            repository.insertNote(note)
            Toast.makeText(this, R.string.msg_note_created, Toast.LENGTH_SHORT).show()
        } else {
            repository.updateNote(note)
            Toast.makeText(this, R.string.msg_note_updated, Toast.LENGTH_SHORT).show()
        }
        closeDrawer()
        refreshCurrentScreen()
    }

    private fun deleteCurrentNote() {
        val note = currentEditingNote ?: return
        repository.deleteNote(note.id!!)
        Toast.makeText(this, R.string.msg_note_deleted, Toast.LENGTH_SHORT).show()
        closeDrawer()
        refreshCurrentScreen()
    }

    private fun shareCurrentNote() {
        val note = currentEditingNote ?: return
        val shareBody = buildString {
            append(note.title)
            if (!note.content.isNullOrEmpty()) {
                append("\n\n")
                append(note.content)
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, note.title)
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, getString(R.string.action_share_note)))
    }

    private fun refreshCurrentScreen() {
        val frag = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (frag) {
            is HomeFragment -> frag.refresh()
            is SearchFragment -> frag.refresh()
            is CategoriesFragment -> frag.refresh()
        }
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.post {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    // Callbacks from fragments

    override fun onNoteSelected(note: Note) {
        openNoteDrawer(note)
    }

    override fun onAddCategoryRequested() {
        categoriesFragment.promptAddCategory()
    }

    override fun onCategorySelected(category: Category) {
        homeFragment.setFilterCategory(category)
        switchFragment(R.id.menu_home)
    }

    // PUBLIC_INTERFACE
    fun selectBottomTab(@IdRes itemId: Int) {
        /** Programmatically select a bottom navigation tab. */
        bottomNav.selectedItemId = itemId
    }
}
