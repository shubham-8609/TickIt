package com.codeleg.tickit.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthViewModel: ViewModel(){

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    val fireBaseDB by lazy { FirebaseDatabase.getInstance().reference }
    fun signUp(email: String, password: String, onResult: (Boolean, String?) ->Unit){
     firebaseAuth.createUserWithEmailAndPassword(email, password)
         .addOnCompleteListener { task ->
             if(task.isSuccessful){
                 onResult(true, null)
             } else {
                 onResult(false, task.exception?.localizedMessage)
             }
         }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit){
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }


}