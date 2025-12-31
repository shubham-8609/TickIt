package com.codeleg.tickit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseDatabase.getInstance().reference

    suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("User ID not found"))

            val userData = mapOf(
                "username" to username,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            firebaseDB.child("users")
                .child(uid)
                .setValue(userData)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }


    fun mapFirebaseError(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException ->
                "Invalid email or password"

            is FirebaseAuthUserCollisionException ->
                "Email already exists"

            is FirebaseAuthWeakPasswordException ->
                "Password is too weak"

            else -> e.localizedMessage ?: "Something went wrong"
        }
    }


    fun isUserLoggedIn(): Boolean =
        firebaseAuth.currentUser != null
}
