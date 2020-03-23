package net.avantica.whereisthetruck.ui.signup

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpViewModel : ViewModel() {
    val onSignUp by lazy { MutableLiveData<FirebaseUser>() }
    val onSignUpFailed by lazy { MutableLiveData<String?>() }
    val isLoading by lazy { MutableLiveData<Boolean>() }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUpUser(email: String, password: String, name: String, neighborhood: String) {
        isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user == null) {
                        onSignUpFailed.value = null
                    } else {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user.updateProfile(profileUpdates) .addOnCompleteListener {
                                onSignUp.value = user
                            }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    onSignUpFailed.value = task.exception?.localizedMessage
                }
            }
    }
}
