package com.codeleg.tickit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel: ViewModel() {

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    val fireBaseDB by lazy { FirebaseDatabase.getInstance().reference }
    fun signUp(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = task.result.user?.uid
                    val userData = mapOf(
                        "username" to username,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    if (uid != null) {
                        fireBaseDB.child("users").child(uid).setValue(userData)
                            .addOnCompleteListener {
                                if (!it.isSuccessful) {
                                    Log.d(
                                        "AuthViewModel",
                                        "Error saving user data: ${it.exception?.localizedMessage}"
                                    )
                                }
                            }
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.localizedMessage)
                    }
                }
            }
    }

        fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.localizedMessage)
                    }
                }
        }
    fun isUserLoggedIn() =  firebaseAuth.currentUser != null



}