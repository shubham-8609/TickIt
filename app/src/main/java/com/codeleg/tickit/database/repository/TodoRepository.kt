package com.codeleg.tickit.database.repository

import com.codeleg.tickit.database.model.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TodoRepository {
    private val firebaseDB = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private fun uid(): String? = auth.currentUser?.uid


    fun observeTodos(): Flow<List<Todo>> = callbackFlow {
        val userId = uid() ?: run {
            close(IllegalStateException("User not logged in"))
            return@callbackFlow
        }

        val todosRef = firebaseDB
            .getReference("todos")
            .child(userId)
            .orderByChild("createdAt")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todos = snapshot.children
                    .mapNotNull { it.getValue(Todo::class.java) }
                    .sortedByDescending { it.createdAt } // optional

                trySend(todos).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        todosRef.addValueEventListener(listener)

        // 🔥 VERY IMPORTANT
        awaitClose {
            todosRef.removeEventListener(listener)
        }
    }


    suspend fun addTodo(todo: Todo) {
        val userId = uid() ?: throw Exception("User not logged in")
        val ref = firebaseDB.getReference("todos").child(userId)
        val id = ref.push().key ?: throw Exception("ID generation failed")
        ref.child(id).setValue(todo.copy(id = id)).await()
    }

    suspend fun updateTodo(todo: Todo): Result<Unit> {
        return try {
            val userId = uid()
                ?: return Result.failure(Exception("User not logged in"))

            val updates = mapOf(
                "title" to todo.title,
                "completed" to todo.completed,
                "priority" to todo.priority,
                "updatedAt" to System.currentTimeMillis()
            )

            firebaseDB.getReference("todos")
                .child(userId)
                .child(todo.id)
                .updateChildren(updates)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateTodoCompletion(todoId: String, completed: Boolean) {
        val userId = uid() ?: throw Exception("User not logged in")

        firebaseDB.getReference("todos").child(userId).child(todoId).updateChildren(
            mapOf(
                "completed" to completed,
                "updatedAt" to System.currentTimeMillis()
            )
        )
            .await()
    }

    suspend fun deleteTodo(todoId: String): Result<Unit> {
        return try {
            val userId = uid() ?: return Result.failure(Exception("User not logged in"))

            firebaseDB.getReference("todos")
                .child(userId)
                .child(todoId)
                .removeValue()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllTodos(): Result<Unit> {
        return try {
            val userId = uid()
                ?: return Result.failure(Exception("User not logged in"))

            firebaseDB.getReference("todos")
                .child(userId)
                .removeValue()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIncompleteCount(): Int {
        val userId = uid() ?: throw Exception("User not logged in")

        val snapshot = firebaseDB.getReference("todos").child(userId)
            .orderByChild("completed").equalTo(false).get().await()
        return snapshot.children.count()
    }



}