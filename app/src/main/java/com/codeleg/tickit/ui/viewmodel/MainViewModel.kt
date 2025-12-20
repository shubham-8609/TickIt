package com.codeleg.tickit.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeleg.tickit.database.model.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainViewModel: ViewModel() {
    val firebaseDB by lazy { FirebaseDatabase.getInstance() }
    private val _allTodos = MutableLiveData<List<Todo>>()
    val allTodos: LiveData<List<Todo>> get() =  _allTodos


    fun addTodo(todo: Todo , onResult: (Boolean,Todo? ,  String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if(uid==null){
            onResult(false, null,"User not logged in")
            return
        }
        val todosRef = firebaseDB.getReference("todos").child(uid)
        val todoId = todosRef.push().key?: run {
            onResult(false, null,"Could not generate todo ID")
            return
        }
        val todoWithId = todo.copy(id = todoId)
        todosRef.child(todoId).setValue(todoWithId)
            .addOnSuccessListener {
                // Update local list
                val current = _allTodos.value.orEmpty()
                _allTodos.value = current + todoWithId
                onResult(true , todoWithId, null)
            }
            .addOnFailureListener {
                onResult(false, null  , it.localizedMessage)
            }
    }

    fun loadTodos(){

    }
}