package com.codeleg.tickit.ui.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeleg.tickit.database.model.Todo
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {
    val firebaseDB by lazy { FirebaseDatabase.getInstance() }
    private val _allTodos = MutableLiveData<List<Todo>>()
    val allTodos: LiveData<List<Todo>> get() = _allTodos
    private val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid
    private var todosListener: ValueEventListener? = null
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username



    suspend fun addTodo(todo: Todo): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            val todosRef = firebaseDB.getReference("todos").child(currentUid)
            val todoId = todosRef.push().key
                ?: return Result.failure(Exception("Could not generate todo ID"))

            val todoWithId = todo.copy(id = todoId)

            todosRef.child(todoId).setValue(todoWithId).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun updateTodoComplete(
        todoId: String,
        isChecked: Boolean
    ): Result<Unit> {
        val currentUid = uid ?: return Result.failure(Exception("User not logged in"))

        return try {
            val updates = mapOf(
                "completed" to isChecked,
                "updatedAt" to System.currentTimeMillis()
            )

            firebaseDB.getReference("todos")
                .child(currentUid)
                .child(todoId)
                .updateChildren(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearTodosListener() {
        val currentUid = uid ?: return
        todosListener?.let {
            firebaseDB
                .getReference("todos")
                .child(currentUid)
                .removeEventListener(it)
        }
    }

    fun deleteTodo(todoId: String, onResult: (Boolean, String?) -> Unit) {
        val todosRef = firebaseDB.getReference("todos")
            .child(uid ?: return onResult(false, "User not logged in")).child(todoId)
        todosRef.removeValue()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun updateTodo(todo: Todo, onResult: (Boolean, String?) -> Unit) {
        val todosRef = firebaseDB.getReference("todos")
            .child(uid ?: return onResult(false, "User not logged in")).child(todo.id)
        val updates = mapOf(
            "title" to todo.title,
            "completed" to todo.completed,
            "updatedAt" to System.currentTimeMillis(),
            "priority" to todo.priority
        )
        todosRef.updateChildren(updates)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
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

    fun deleteAllTodos(onResult: (Boolean, String?) -> Unit) {
        val todosRef = firebaseDB.getReference("todos")
            .child(uid ?: return onResult(false, "User not logged in"))
        todosRef.removeValue()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
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


    private fun mapFirebaseError(e: Exception): String {
        return when (e) {

            is FirebaseAuthRecentLoginRequiredException ->
                "Please log in again to change your password"

            is FirebaseAuthWeakPasswordException ->
                "New password must be at least 6 characters"

            is FirebaseAuthInvalidCredentialsException ->
                "Re-authentication failed. Please verify your password."

            else ->
                "Something went wrong. Please try again."
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
