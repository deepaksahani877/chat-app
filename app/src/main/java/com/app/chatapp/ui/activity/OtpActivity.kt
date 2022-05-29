package com.app.chatapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.chatapp.R
import com.app.chatapp.databinding.ActivityOtpBinding
import com.app.chatapp.ui.dialogs.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var verificationID: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber:String
    private lateinit var loadingDialog :LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        loadingDialog = LoadingDialog(resources.getString(R.string.sending_otp))

        binding.included.toolbar.title = "Otp Verification"

        phoneNumber = intent.getStringExtra("phoneNumber")!!
        binding.otpSentToTextView.text =
            String.format(resources.getString(R.string.otp_code_input_hint), phoneNumber)
        binding.verifyButton.setOnClickListener {
            verifyCode(binding.otpView.otp)
        }
        if (auth.currentUser == null) {
            sendVerificationCode(phoneNumber)
        }

    }


    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            finish()
        }
    }


    private fun sendVerificationCode(phoneNumber: String) {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callBacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private val callBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            //makeToast("verification completed")
            if (credential.smsCode != null) {
                binding.otpView.otp = credential.smsCode
                signInWithPhoneAuthCredential(credential)
            }

        }

        override fun onVerificationFailed(e: FirebaseException) {

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
               // makeToast("Invalid request")
                Snackbar.make(binding.otpView,"Invalid request", Snackbar.LENGTH_LONG).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                makeToast("sms quota exceeded")
            }
            loadingDialog.dismiss()
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            loadingDialog.dismiss()
            makeToast("Code sent")
            verificationID = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                //makeToast("signIn with credential")
                if (task.isSuccessful) {
                    startActivity(Intent(applicationContext,UserNameUpdate::class.java))
                    binding.progressBar.visibility = View.INVISIBLE
                    finish()
                } else {

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        binding.progressBar.visibility = View.INVISIBLE
                        Snackbar.make(binding.otpView,"Invalid verification code", Snackbar.LENGTH_LONG).show()
                    }
                }
                loadingDialog.dismiss()
            }
    }


    private fun verifyCode(verificationCode: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential = PhoneAuthProvider.getCredential(verificationID, verificationCode)
        signInWithPhoneAuthCredential(credential)
    }

    private fun makeToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}