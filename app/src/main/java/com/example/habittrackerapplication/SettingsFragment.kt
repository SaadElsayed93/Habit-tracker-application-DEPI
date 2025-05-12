package com.example.habittrackerapplication

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.habittrackerapplication.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // زر تغيير الاسم
        binding.buttonChangeName.setOnClickListener {
            // هنا يمكنك إضافة وظيفة لتغيير الاسم مثل عرض حوار لتغيير الاسم
        }

        // زر تغيير كلمة المرور
        binding.buttonChangePassword.setOnClickListener {
            // هنا يمكنك إضافة وظيفة لتغيير كلمة المرور
        }

        // زر تغيير اللغة
        binding.buttonLanguage.setOnClickListener {
            // يمكنك إضافة وظيفة لتغيير اللغة
        }

        // تفعيل أو تعطيل الإشعارات
        val switchNotifications: Switch = binding.switchNotifications
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // هنا يمكنك حفظ تفعيل/تعطيل الإشعارات حسب الحالة
        }

        // التبديل بين Dark Mode و Light Mode
        val switchDarkMode: Switch = binding.switchDarkMode
        switchDarkMode.isChecked = isDarkMode()
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // زر تسجيل الخروج
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // إغلاق النشاط الحالي بعد تسجيل الخروج
        }
    }

    private fun isDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
