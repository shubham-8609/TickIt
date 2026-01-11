package com.codeleg.tickit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeleg.tickit.utils.AuthUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseDatabase.getInstance().reference
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState

     fun signUp(
        username: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .await()
                val uid = authResult.user?.uid ?: throw Exception("User ID not found")
                val userData = mapOf(
                    "username" to username,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                firebaseDB.child("users")
                    .child(uid)
                    .setValue(userData)
                    .await()

                _authState.value = AuthUiState.Success

            }
            catch (e: Exception) {
                _authState.value =
                    AuthUiState.Error(mapFirebaseError(e))
            }
        }

    }

    suspend fun sendPassResetLink(email: String): Result<Unit> {
        return try {
            firebaseAuth
                .sendPasswordResetEmail(email)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?:"Error to get error :)")
            }
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
