package com.codeleg.tickit.utils

import com.codeleg.tickit.database.model.Todo



sealed class TodosUiState {
    object Loading : TodosUiState()
    data class Success(val todos: List<Todo>) : TodosUiState()
    data class Error(val message: String) : TodosUiState()
}
