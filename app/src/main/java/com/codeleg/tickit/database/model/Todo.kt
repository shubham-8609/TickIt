package com.codeleg.tickit.database.model

data class Todo(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val priority : Int = 0
)