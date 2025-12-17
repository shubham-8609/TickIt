package com.codeleg.tickit.database.model

data class Todo(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)