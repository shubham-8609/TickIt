package com.codeleg.tickit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codeleg.tickit.database.repository.TodoRepository
import com.codeleg.tickit.database.repository.UserRepository

class MainViewModelFactory(
    private val todoRepo: TodoRepository,
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(todoRepo , userRepo ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}