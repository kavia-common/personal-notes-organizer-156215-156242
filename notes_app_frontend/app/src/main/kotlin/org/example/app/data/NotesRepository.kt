package org.example.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * PUBLIC_INTERFACE
 * Repository providing CRUD operations for Notes and Categories.
 */
class NotesRepository(context: Context) {

    private val dbHelper: NotesDbHelper = NotesDbHelper(context.applicationContext)

    private val db: SQLiteDatabase
        get() = dbHelper.writableDatabase

    // Notes

    // PUBLIC_INTERFACE
    fun getAllNotes(categoryId: Long? = null): List<Note> {
        /** Returns all notes, optionally filtered by categoryId. */
        val where = if (categoryId != null) "category_id=?" else null
        val args = if (categoryId != null) arrayOf(categoryId.toString()) else null
        val cursor = db.query(
            "notes",
            arrayOf("id", "title", "content", "category_id", "created_at", "updated_at"),
            where,
            args,
            null,
            null,
            "updated_at DESC"
        )
        cursor.use { return readNotes(cursor) }
    }

    // PUBLIC_INTERFACE
    fun searchNotes(query: String): List<Note> {
        /** Returns notes whose title or content contains the query string (case-insensitive). */
        val like = "%${query.replace("%", "\\%").replace("_", "\\_")}%"
        val cursor = db.query(
            "notes",
            arrayOf("id", "title", "content", "category_id", "created_at", "updated_at"),
            "title LIKE ? ESCAPE '\\' OR content LIKE ? ESCAPE '\\'",
            arrayOf(like, like),
            null,
            null,
            "updated_at DESC"
        )
        cursor.use { return readNotes(cursor) }
    }

    // PUBLIC_INTERFACE
    fun insertNote(note: Note): Long {
        /** Inserts a note and returns the new ID. */
        val values = ContentValues().apply {
            put("title", note.title)
            put("content", note.content)
            if (note.categoryId != null) put("category_id", note.categoryId) else putNull("category_id")
            put("created_at", note.createdAt)
            put("updated_at", note.updatedAt)
        }
        return db.insertOrThrow("notes", null, values)
    }

    // PUBLIC_INTERFACE
    fun updateNote(note: Note): Int {
        /** Updates an existing note; returns the rows affected. */
        requireNotNull(note.id)
        val values = ContentValues().apply {
            put("title", note.title)
            put("content", note.content)
            if (note.categoryId != null) put("category_id", note.categoryId) else putNull("category_id")
            put("updated_at", note.updatedAt)
        }
        return db.update("notes", values, "id=?", arrayOf(note.id.toString()))
    }

    // PUBLIC_INTERFACE
    fun deleteNote(id: Long) {
        /** Deletes the note by ID. */
        db.delete("notes", "id=?", arrayOf(id.toString()))
    }

    private fun readNotes(cursor: Cursor): List<Note> {
        val out = ArrayList<Note>()
        while (cursor.moveToNext()) {
            out.add(
                Note(
                    id = cursor.getLong(0),
                    title = cursor.getString(1),
                    content = cursor.getString(2),
                    categoryId = if (cursor.isNull(3)) null else cursor.getLong(3),
                    createdAt = cursor.getLong(4),
                    updatedAt = cursor.getLong(5)
                )
            )
        }
        return out
    }

    // Categories

    // PUBLIC_INTERFACE
    fun getAllCategories(): List<Category> {
        /** Returns all categories sorted by name. */
        val cursor = db.query(
            "categories",
            arrayOf("id", "name"),
            null,
            null,
            null,
            null,
            "LOWER(name) ASC"
        )
        val out = ArrayList<Category>()
        cursor.use {
            while (cursor.moveToNext()) {
                out.add(Category(cursor.getLong(0), cursor.getString(1)))
            }
        }
        return out
    }

    // PUBLIC_INTERFACE
    fun insertCategory(name: String): Long {
        /** Inserts a category with the given name and returns the ID. If exists, returns existing ID. */
        // Try find existing
        val cur = db.query("categories", arrayOf("id"), "LOWER(name)=LOWER(?)", arrayOf(name), null, null, null)
        cur.use {
            if (it.moveToFirst()) return it.getLong(0)
        }
        val values = ContentValues().apply { put("name", name.trim()) }
        return db.insertOrThrow("categories", null, values)
    }

    // PUBLIC_INTERFACE
    fun deleteCategory(id: Long) {
        /** Deletes a category and nulls category_id in notes via foreign key rule. */
        db.delete("categories", "id=?", arrayOf(id.toString()))
        // category_id in notes becomes NULL because of FK ON DELETE SET NULL
    }

    // PUBLIC_INTERFACE
    fun renameCategory(id: Long, newName: String): Int {
        /** Renames a category; returns rows affected. */
        val values = ContentValues().apply { put("name", newName.trim()) }
        return db.update("categories", values, "id=?", arrayOf(id.toString()))
    }

    // PUBLIC_INTERFACE
    fun getCategoryNameById(id: Long?): String {
        /** Returns category name by ID; if null or not found, returns "None". */
        if (id == null) return "None"
        val cursor = db.query("categories", arrayOf("name"), "id=?", arrayOf(id.toString()), null, null, null)
        cursor.use {
            if (it.moveToFirst()) return it.getString(0)
        }
        return "None"
    }
}
