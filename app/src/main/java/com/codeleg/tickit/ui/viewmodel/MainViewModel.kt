package com.codeleg.tickit.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeleg.tickit.database.model.Todo
import com.codeleg.tickit.database.repository.TodoRepository
import com.codeleg.tickit.database.repository.UserRepository
import com.codeleg.tickit.utils.TodosUiState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(val todoRepo: TodoRepository , val userRepo: UserRepository) : ViewModel() {
    val firebaseDB by lazy { FirebaseDatabase.getInstance() }
    val todosUiState: StateFlow<TodosUiState> =
        todoRepo.observeTodos()
            .map<List<Todo>, TodosUiState> { todos ->
                TodosUiState.Success(todos)
            }
            .onStart {
                emit(TodosUiState.Loading)
            }
            .catch { e ->
                emit(TodosUiState.Error(e.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodosUiState.Loading
            )
    private val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username



     fun addTodo(todo: Todo) = viewModelScope.launch { todoRepo.addTodo(todo) }

     fun updateTodoComplete(todoId: String, isChecked: Boolean) = viewModelScope.launch { todoRepo.updateTodoCompletion(todoId , isChecked) }

    suspend fun deleteTodo(todoId: String): Result<Unit> {
        return todoRepo.deleteTodo(todoId)
    }


    suspend fun updateTodo(todo: Todo): Result<Unit> {
        return todoRepo.updateTodo(todo)
    }


    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    fun loadUsername() {
        viewModelScope.launch {
            userRepo.loadUsername().fold(
                onSuccess = { username ->
                    _username.value = username
                },
                onFailure = {
                    _username.value = ""
                }
            )
        }
    }


    fun logout() {
        userRepo.logout()
    }

    suspend fun deleteAllTodos(): Result<Unit> {
        return todoRepo.deleteAllTodos()
    }


    suspend fun updatePassword(
        oldPass: String,
        newPass: String
    ): Result<Unit> {
        return userRepo.updatePassword(oldPass, newPass)
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(userRepo.deleteAccount())
        }
    }


}
