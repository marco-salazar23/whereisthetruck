package net.avantica.whereisthetruck.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.login_fragment.*
import net.avantica.whereisthetruck.MainActivity
import net.avantica.whereisthetruck.R

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private val loadingObserver = Observer<Boolean> {
        progressBar.visibility = if(it) View.VISIBLE else View.INVISIBLE
    }

    private val onLoginObserver = Observer<FirebaseUser> {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private val onLoginFailedObserver = Observer<String?> {
        Toast.makeText(context, getText(R.string.invalid_login), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        signUpButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginToSignup()
            Navigation.findNavController(it).navigate(action)
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.enter_login_info), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.logUser(email, password)
            }

        }

        viewModel.isLoading.observe(this, loadingObserver)
        viewModel.onLogin.observe(this, onLoginObserver)
        viewModel.onLoginFailed.observe(this, onLoginFailedObserver)
        viewModel.checkUser()
    }

}
