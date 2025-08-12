package org.example.app.data

/**
 * PUBLIC_INTERFACE
 * Data class representing a note.
 */
data class Note(
    val id: Long?,
    val title: String,
    val content: String?,
    val categoryId: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * PUBLIC_INTERFACE
 * Data class representing a category.
 */
data class Category(
    val id: Long,
    val name: String
)
