package com.app.chatapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.chatapp.R
import com.app.chatapp.databinding.ActivityGetStartedBinding
import com.google.firebase.auth.FirebaseAuth

class GetStartedActivity:AppCompatActivity() {
    private lateinit var binding:ActivityGetStartedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAgreementText()
        binding.getStartedButton.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }


    }

    //By getting Started you agree to our terms &amp; conditions.

    private fun setAgreementText(){
        val str = resources.getString(R.string.agreement_text)
        val spannableTermsAndConditionsText = SpannableString(str)
        val clickableTermsText = object : ClickableSpan(){
            override fun onClick(widget: View) {
                Toast.makeText(applicationContext,"clicked on terms", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(applicationContext,R.color.skyBlue)
                ds.isUnderlineText = false
            }
        }
        val clickableConditionsText = object : ClickableSpan(){
            override fun onClick(widget: View) {
                Toast.makeText(applicationContext,"clicked on Conditions", Toast.LENGTH_SHORT).show()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(applicationContext,R.color.skyBlue)
                ds.isUnderlineText = false
            }
        }
        spannableTermsAndConditionsText.setSpan(clickableTermsText,36,41,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableTermsAndConditionsText.setSpan(clickableConditionsText,44,54,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.agreementTextView.text = spannableTermsAndConditionsText
        binding.agreementTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser!=null){
            finish()
        }
    }
}