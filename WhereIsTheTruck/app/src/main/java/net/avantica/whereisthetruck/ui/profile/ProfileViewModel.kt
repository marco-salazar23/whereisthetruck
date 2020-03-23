package net.avantica.whereisthetruck.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val user = auth.currentUser!!
    val onPasswordUpdated by lazy { MutableLiveData<Boolean>() }
    val onPasswordUpdateFailed by lazy { MutableLiveData<String?>() }
    val isLoading by lazy { MutableLiveData<Boolean>() }
    val onUserProfileImage by lazy { MutableLiveData<Bitmap>() }
    private val imageReference: StorageReference? = FirebaseStorage.getInstance().reference.child("images").child(user.uid)

    fun updatePassword(currentPass: String, newPassword: String) {
      isLoading.value = true
        reloginUser(user.email.toString(), currentPass) { result, errorMessage ->
            if (result) {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    isLoading.value = false
                    if (task.isSuccessful) {
                        onPasswordUpdated.value = true
                    } else {
                        onPasswordUpdateFailed.value = task.exception?.localizedMessage
                    }
                }
            } else {
                isLoading.value = false
                onPasswordUpdateFailed.value = errorMessage
            }
        }
    }

    private fun reloginUser(email: String, pass: String, onComplete: (result: Boolean, message: String?)-> Unit) {

        val credential = EmailAuthProvider
            .getCredential(email, pass)

        user.reauthenticate(credential).addOnCompleteListener {
                onComplete(it.isSuccessful, it.exception?.localizedMessage)
            }
    }

    fun saveImage(image: Bitmap) {
        isLoading.value = true
        GlobalScope.launch {

            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            var uploadTask = imageReference?.putBytes(data)
            uploadTask?.addOnFailureListener {
                isLoading.value = false
            }?.addOnSuccessListener {
                isLoading.value = false
                onUserProfileImage.value = image
            }
        }

    }

    fun checkImage(){

        val ONE_MEGABYTE: Long = 1024 * 1024
        imageReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            onUserProfileImage.value = BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

}
