package com.codeleg.tickit.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeleg.tickit.database.model.Todo
import com.codeleg.tickit.database.repository.TodoRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(val todoRepo: TodoRepository) : ViewModel() {
    val firebaseDB by lazy { FirebaseDatabase.getInstance() }
    private val _allTodos = MutableLiveData<List<Todo>>()
    val allTodos: LiveData<List<Todo>> get() = _allTodos
    private val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid
    private var todosListener: ValueEventListener? = null
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username



     fun addTodo(todo: Todo) = viewModelScope.launch { todoRepo.addTodo(todo) }

    fun loadTodos(onComplete: (Boolean, String?) -> Unit) {
        val currentUid = uid ?: return onComplete(false, "User not logged in")
        val todosRef =
            firebaseDB.getReference("todos").child(uid ?: return).orderByChild("createdAt")
        todosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _allTodos.value =snapshot.children.mapNotNull { it.getValue(Todo::class.java) }
                onComplete(true, null)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }

        }
        todosRef.addValueEventListener(todosListener!!)
    }

     fun updateTodoComplete(todoId: String, isChecked: Boolean) = viewModelScope.launch { todoRepo.updateTodoCompletion(todoId , isChecked) }

    fun clearTodosListener() {
        val currentUid = uid ?: return
        todosListener?.let {
            firebaseDB
                .getReference("todos")
                .child(currentUid)
                .removeEventListener(it)
        }
    }

    suspend fun deleteTodo(todoId: String): Result<Unit> {
        return todoRepo.deleteTodo(todoId)
    }


    suspend fun updateTodo(todo: Todo): Result<Unit> {
        return todoRepo.updateTodo(todo)
    }


    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    fun loadUsername() {
        firebaseDB.getReference("users").child(uid ?: return).get().addOnSuccessListener {
            _username.value = it.child("username").value?.toString() ?: ""
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun deleteAllTodos(): Result<Unit> {
        return todoRepo.deleteAllTodos()
    }


    suspend fun udpatePass(
        oldPass: String,
        newPass: String
    ): Result<Unit> {
        val user = getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val credential = EmailAuthProvider
                .getCredential(user.email!!, oldPass)

            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))
        val currentUid = uid ?: return Result.failure(Exception("User not logged in"))

        return try {
            firebaseDB.getReference("users").child(currentUid).removeValue().await()
            firebaseDB.getReference("todos").child(currentUid).removeValue().await()
            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearTodosListener()
    }

}
