package net.avantica.whereisthetruck.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.sign_up_fragment.*
import net.avantica.whereisthetruck.MainActivity
import net.avantica.whereisthetruck.R

class SignUpFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var viewModel: SignUpViewModel
    private val loadingObserver = Observer<Boolean> {
        progressBar.visibility = if(it) View.VISIBLE else View.INVISIBLE
    }

    private val onSignUpObserver = Observer<FirebaseUser> {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private val onSignUpFailedObserver = Observer<String?> { message ->
        if (message == null) {
            Toast.makeText(context, getText(R.string.generic_error), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sign_up_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignUpViewModel::class.java)

        signupButton.setOnClickListener {
            val email = emailTextfield.text.toString()
            val password = passwordTextfield.text.toString()
            val repass = confirmationPassTextfield.text.toString()
            val name = nameTextfield.text.toString()
            val neighborhood = neighborhoodTextField.text.toString()


            if (email.isNullOrEmpty() || password.isNullOrEmpty() || repass.isNullOrEmpty() || name.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.all_fields_are_mandatory), Toast.LENGTH_SHORT).show()
            } else if (password != repass) {
                Toast.makeText(context, getString(R.string.password_dont_match), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.signUpUser(email, password, name, neighborhood)
            }

        }

        viewModel.isLoading.observe(this, loadingObserver)
        viewModel.onSignUp.observe(this, onSignUpObserver)
        viewModel.onSignUpFailed.observe(this, onSignUpFailedObserver)
    }

}
