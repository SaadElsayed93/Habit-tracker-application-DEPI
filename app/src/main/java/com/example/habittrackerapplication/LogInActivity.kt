package com.example.habittrackerapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habittrackerapplication.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val sharedPrefs by lazy {
        getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // تحميل بيانات "تذكرني" إن وُجدت
        loadSavedCredentials()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()

                            // حفظ بيانات تسجيل الدخول إذا تم اختيار "تذكرني"
                            if (binding.rememberMeCheckBox.isChecked) {
                                sharedPrefs.edit().apply {
                                    putString("email", email)
                                    putString("password", password)
                                    putBoolean("remember", true)
                                    apply()
                                }
                            } else {
                                sharedPrefs.edit().clear().apply()
                            }

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "فشل تسجيل الدخول: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "الرجاء إدخال البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.forgetPasswordLink.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "حدث خطأ: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "الرجاء إدخال البريد الإلكتروني أولاً", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSavedCredentials() {
        val remember = sharedPrefs.getBoolean("remember", false)
        if (remember) {
            binding.emailEditText.setText(sharedPrefs.getString("email", ""))
            binding.passwordEditText.setText(sharedPrefs.getString("password", ""))
            binding.rememberMeCheckBox.isChecked = true
        }
    }
}
