package com.codeleg.tickit.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeleg.tickit.database.model.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel : ViewModel() {
    val firebaseDB by lazy { FirebaseDatabase.getInstance() }
    private val _allTodos = MutableLiveData<List<Todo>>()
    val allTodos: LiveData<List<Todo>> get() = _allTodos
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    private var todosListener: ValueEventListener? = null

    fun addTodo(todo: Todo, onResult: (Boolean, String?) -> Unit) {
        if (uid == null) {
            onResult(false, "User not logged in")
            return
        }
        val todosRef = firebaseDB.getReference("todos").child(uid)
        val todoId = todosRef.push().key ?: run {
            onResult(false, "Could not generate todo ID")
            return
        }
        val todoWithId = todo.copy(id = todoId)
        todosRef.child(todoId).setValue(todoWithId)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.localizedMessage)
            }
    }

    fun loadTodos(onComplete: (Boolean, String?) -> Unit) {
        val todosRef =
            firebaseDB.getReference("todos").child(uid ?: return).orderByChild("createdAt")
        todosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todos = snapshot.children.mapNotNull { it.getValue(Todo::class.java) }
                _allTodos.value = todos
                onComplete(true, null)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }

        }
        todosRef.addValueEventListener(todosListener!!)
    }

    fun updateTodoComplete(todoId: String , isChecked: Boolean , onResult: (Boolean, String?) -> Unit){
        val todosRef = firebaseDB.getReference("todos").child(uid?:return onResult(false , "User not logged in")).child(todoId)
        val updates = mapOf(
            "completed" to isChecked,
            "updatedAt" to System.currentTimeMillis()
        )
        todosRef.updateChildren(updates)
            .addOnSuccessListener { onResult(true , null) }
            .addOnFailureListener { onResult(false , it.localizedMessage
            ) }

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

    fun deleteTodo(todoId: String , onResult: (Boolean, String?) ->Unit){
        val todosRef = firebaseDB.getReference("todos").child(uid?:return onResult(false , "User not logged in")).child(todoId)
        todosRef.removeValue()
            .addOnSuccessListener { onResult(true , null) }
            .addOnFailureListener { onResult(false , it.localizedMessage) }
    }

    fun updateTodo(todo: Todo , onResult: (Boolean, String?) ->Unit){
        val todosRef = firebaseDB.getReference("todos").child(uid?:return onResult(false , "User not logged in")).child(todo.id)
        val updates = mapOf(
            "title" to todo.title,
            "completed" to todo.completed,
            "updatedAt" to System.currentTimeMillis(),
            "priority" to todo.priority
        )
        todosRef.updateChildren(updates)
            .addOnSuccessListener { onResult(true , null) }
            .addOnFailureListener { onResult(false , it.localizedMessage) }
    }

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    fun getUsername() : String{
        var username  = ""
        firebaseDB.getReference("users").child(uid!!).get().addOnSuccessListener {
             username = it.child("username").value.toString()
        }
        return username
    }
    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun deleteAllTodos(onResult: (Boolean, String?) ->Unit){
        val todosRef = firebaseDB.getReference("todos").child(uid?:return onResult(false , "User not logged in"))
        todosRef.removeValue()
            .addOnSuccessListener { onResult(true , null) }
            .addOnFailureListener { onResult(false , it.localizedMessage) }
    }

    override fun onCleared() {
        super.onCleared()
        clearTodosListener()
    }

}
