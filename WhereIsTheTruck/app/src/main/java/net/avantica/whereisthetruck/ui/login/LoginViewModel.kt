package net.avantica.whereisthetruck.ui.login

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {
    val onLogin by lazy { MutableLiveData<FirebaseUser>() }
    val onLoginFailed by lazy { MutableLiveData<String?>() }
    val isLoading by lazy { MutableLiveData<Boolean>() }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onLogin.value = currentUser
        }
    }

    fun logUser(email: String, password: String) {
        isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                isLoading.value = false
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    onLogin.value = user
                } else {
                    Log.w(ContentValues.TAG, "signInWithEmail:failure", it.exception)
                    onLoginFailed.value = it.exception?.localizedMessage
                }
            }
    }
}
