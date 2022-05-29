package com.app.chatapp.ui.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.chatapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isPhoneNumberValid = false
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.included.toolbar.title = "Phone verification"

        setSupportActionBar(binding.included.toolbar)

        binding.sendOtpFAB.setOnClickListener {
            if (isPhoneNumberValid) {
                if (isNetworkAvailable()) {
                    phoneNumber = "+91" + binding.phoneTextInputEditText.text.toString()
                    val intent = Intent(this, OtpActivity::class.java)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)

                } else {
                    Toast.makeText(
                        applicationContext,
                        "You are not connected to the internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (binding.phoneTextInputEditText.text.toString() == "") {
                binding.phoneTextInputLayout.error = "Required"
                binding.phoneTextInputEditText.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    binding.phoneTextInputEditText,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        }

        binding.phoneTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    binding.phoneTextInputEditText.text.toString().isEmpty() -> {
                        binding.phoneTextInputLayout.error = "Required"
                        isPhoneNumberValid = false
                    }
                    binding.phoneTextInputEditText.text.toString().length != 10 -> {
                        binding.phoneTextInputLayout.error = "Invalid Phone Number"
                        isPhoneNumberValid = false
                    }
                    else -> {
                        binding.phoneTextInputLayout.isErrorEnabled = false
                        isPhoneNumberValid = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
           startActivity(Intent(applicationContext,MainActivity::class.java))
            finish()
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            } else {
                null
            }
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

}