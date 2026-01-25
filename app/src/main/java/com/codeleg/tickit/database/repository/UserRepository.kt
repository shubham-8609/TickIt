package com.codeleg.tickit.database.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private fun uid(): String? = auth.currentUser?.uid

    suspend fun updatePassword(oldPass: String, newPass: String): Result<Unit> {

        val user = auth.currentUser
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val credential = EmailAuthProvider
                .getCredential(user.email ?: return Result.failure(Exception("Email not found")), oldPass)

            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun logout() { auth.signOut() }
    suspend fun loadUsername(): Result<String> {
        val currentUid = uid() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val snapshot = db.getReference("users").child(currentUid).get().await()
            val username = snapshot.child("username").value?.toString().orEmpty()
            Result.success(username)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val currentUid = uid() ?: return Result.failure(Exception("User not logged in"))

        return try {
            // 1️⃣ Delete DB data first
            db.getReference("todos").child(currentUid).removeValue().await()
            db.getReference("users").child(currentUid).removeValue().await()

            // 2️⃣ Delete Auth user
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}