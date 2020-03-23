package net.avantica.whereisthetruck.ui.profile

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.profile_fragment.*
import net.avantica.whereisthetruck.R
import net.avantica.whereisthetruck.databinding.ProfileFragmentBinding
import net.avantica.whereisthetruck.utilities.IMAGE_UPDATED_BROADCAST


class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2
    private var mUri: Uri? = null

    private lateinit var viewModel: ProfileViewModel
    private val loadingObserver = Observer<Boolean> {
        progressBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
    }

    private val onPasswordUpdatedObserver = Observer<Boolean> {
        Toast.makeText(context, R.string.password_updated, Toast.LENGTH_SHORT).show()
    }

    private val onPasswordUpdateFailedObserver = Observer<String?> {
        val message = it ?: getString(R.string.generic_error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private val onImageUpdated = Observer<Bitmap> {
        profileImageButton.setImageBitmap(it)
        val intent = Intent(IMAGE_UPDATED_BROADCAST)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updatePassBtn.setOnClickListener { validatePasswords() }
        profileImageButton.setOnClickListener { updateProfilePhoto() }

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        if (view != null) {
            val binding: ProfileFragmentBinding? = DataBindingUtil.bind<ProfileFragmentBinding>(view!!)
            binding?.user = viewModel.user
        }
        viewModel.onPasswordUpdated.observe(this, onPasswordUpdatedObserver)
        viewModel.isLoading.observe(this, loadingObserver)
        viewModel.onPasswordUpdateFailed.observe(this, onPasswordUpdateFailedObserver)
        viewModel.onUserProfileImage.observe(this, onImageUpdated)
        viewModel.checkImage()
    }

    private fun validatePasswords() {
        val currentPass = currentPasswordTextfield.text.toString()
        val password = newPasswordTextfield.text.toString()
        val confirmPassword = confirmPasswordTextfield.text.toString()

        if (password.isNullOrEmpty() || confirmPassword.isNullOrEmpty() || currentPass.isNullOrEmpty()) {
            Toast.makeText(context, R.string.all_fields_are_mandatory, Toast.LENGTH_SHORT).show()

        } else if (password == confirmPassword) {
            viewModel.updatePassword(currentPass, password)
            currentPasswordTextfield.text.clear()
            newPasswordTextfield.text.clear()
            confirmPasswordTextfield.text.clear()
        } else {
            Toast.makeText(context, R.string.password_dont_match, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfilePhoto() {
        if (context == null) { return }


        AlertDialog.Builder(context!!)
            .setTitle("Select an image")
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            .setNeutralButton("From gallery", DialogInterface.OnClickListener { _, _ -> openGallery() })
            .setPositiveButton("From camera", DialogInterface.OnClickListener{ _, _ -> takePhotoFromCamera() })
            .show()
    }

    private fun openGallery() {
        checkPermissions {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
        }

    }

    private fun takePhotoFromCamera() {
        checkPermissions {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
        }
    }
    
    private fun checkPermissions(onCompletion: () -> Unit) {
        val checkGalleryPermission = ContextCompat.checkSelfPermission(context!!,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val checkCameraPermission = ContextCompat.checkSelfPermission(context!!,
            android.Manifest.permission.CAMERA)
        if (checkGalleryPermission != PackageManager.PERMISSION_GRANTED || checkCameraPermission != PackageManager.PERMISSION_GRANTED){
            //Requests permissions to be granted to this application at runtime
            ActivityCompat.requestPermissions(activity!!,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA), 1)
        }
        else{
            onCompletion()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                OPERATION_CHOOSE_PHOTO -> {
                        profileImageButton.setImageURI(data?.data)
                }
                OPERATION_CAPTURE_PHOTO -> {
                    val thumbnail: Bitmap? = data!!.extras!!["data"] as Bitmap?
                    profileImageButton.setImageBitmap(thumbnail)
                }

            }
            saveImage()
        }

    }

    private fun saveImage() {

        val bitmap = (profileImageButton.drawable as BitmapDrawable).bitmap
        viewModel.saveImage(bitmap)
    }


}
