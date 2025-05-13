package com.example.habittrackerapplication

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.habittrackerapplication.databinding.FragmentSettingsBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", true) // تعيين افتراضيًا للوضع الداكن
        binding.switchDarkMode.isChecked = isDarkMode
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        binding.buttonChangeName.setOnClickListener {
            showChangeNameDialog()
        }

        binding.buttonChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.buttonLanguage.setOnClickListener {
            showLanguageChangeDialog()
        }

        binding.switchNotifications.isChecked = isNotificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference(isChecked)
        }

        binding.switchDarkMode.isChecked = isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showChangeNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.change_name))

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL

        val firstNameInput = EditText(requireContext())
        firstNameInput.hint = getString(R.string.first_name)
        layout.addView(firstNameInput)

        val lastNameInput = EditText(requireContext())
        lastNameInput.hint = getString(R.string.last_name)
        layout.addView(lastNameInput)

        builder.setView(layout)

        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("users").document(uid)
                        .update(mapOf("firstName" to firstName, "lastName" to lastName))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), getString(R.string.name_changed), Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), getString(R.string.error_updating_name), Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_empty_name), Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.change_password))

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL

        val oldPasswordInput = EditText(requireContext())
        oldPasswordInput.hint = getString(R.string.enter_old_password)
        oldPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(oldPasswordInput)

        val newPasswordInput = EditText(requireContext())
        newPasswordInput.hint = getString(R.string.enter_new_password)
        newPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(newPasswordInput)

        val confirmPasswordInput = EditText(requireContext())
        confirmPasswordInput.hint = getString(R.string.confirm_new_password)
        confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(confirmPasswordInput)

        builder.setView(layout)

        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val oldPassword = oldPasswordInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // تحقق من الحقول الفارغة
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.password_cannot_be_empty), Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // تحقق من تطابق الباسورد الجديد مع تأكيد الباسورد
            if (newPassword != confirmPassword) {
                confirmPasswordInput.error = getString(R.string.password_mismatch)
                return@setPositiveButton
            }

            val user = auth.currentUser
            val email = user?.email

            if (email != null) {
                // إعادة المصادقة باستخدام كلمة المرور القديمة
                val credential = EmailAuthProvider.getCredential(email, oldPassword)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        // تحديث كلمة المرور الجديدة
                        user.updatePassword(newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), getString(R.string.password_changed), Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), getString(R.string.error_updating_password), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        oldPasswordInput.error = getString(R.string.incorrect_old_password)
                    }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_empty_password), Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }





    private fun showLanguageChangeDialog() {
        val languageOptions = arrayOf("English", "العربية")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.change_language))
        builder.setItems(languageOptions) { _, which ->
            when (which) {
                0 -> changeLanguage("en")
                1 -> changeLanguage("ar")
            }
        }
        builder.show()
    }

    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        sharedPreferences.edit().putString("language", languageCode).apply()
        Toast.makeText(requireContext(), getString(R.string.language_changed), Toast.LENGTH_SHORT).show()

        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }

    private fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    private fun saveNotificationPreference(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", isEnabled).apply()
    }

    private fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
