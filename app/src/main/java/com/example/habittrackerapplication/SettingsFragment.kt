package com.example.habittrackerapplication

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.habittrackerapplication.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
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

        binding.buttonChangeName.setOnClickListener {
            showChangeNameDialog()
        }

        binding.buttonChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.buttonLanguage.setOnClickListener {
            showLanguageChangeDialog()
        }

        // الإشعارات
        binding.switchNotifications.isChecked = isNotificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference(isChecked)
        }

        // الوضع الليلي
        binding.switchDarkMode.isChecked = isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // تسجيل الخروج
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

        val input = EditText(requireContext())
        input.hint = getString(R.string.enter_new_name)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                // حفظ الاسم الجديد
                Toast.makeText(requireContext(), getString(R.string.name_changed), Toast.LENGTH_SHORT).show()
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

        val input = EditText(requireContext())
        input.hint = getString(R.string.enter_new_password)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val newPassword = input.text.toString().trim()
            if (newPassword.isNotEmpty()) {
                // حفظ كلمة المرور الجديدة
                Toast.makeText(requireContext(), getString(R.string.password_changed), Toast.LENGTH_SHORT).show()
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

        val editor = sharedPreferences.edit()
        editor.putString("language", languageCode)
        editor.apply()

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
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
