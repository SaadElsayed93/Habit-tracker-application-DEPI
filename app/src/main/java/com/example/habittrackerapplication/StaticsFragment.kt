package com.example.habittrackerapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.db.williamchart.view.BarChartView
import com.example.habittrackerapplication.databinding.FragmentStaticsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaticsFragment : Fragment(R.layout.fragment_statics) {

    private var _binding: FragmentStaticsBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStaticsBinding.bind(view)

        loadHabitsFromFirestore() // جلب العادات من Firestore

        setUpChart() // إعداد الـ BarChart
    }

    private fun loadHabitsFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        val habitsRef = db.collection("users").document(uid).collection("habits")

        habitsRef.get()
            .addOnSuccessListener { result ->
                var totalHabits = 0
                var completedHabits = 0
                val habits = mutableListOf<String>()
                result.forEach { document ->
                    totalHabits++
                    val isCompleted = document.getBoolean("isCompleted") ?: false
                    if (isCompleted) {
                        completedHabits++
                    }
                    habits.add(document.getString("habitName") ?: "")
                }

                // تحديث البيانات في الواجهة
                binding.totalHabitsText.text = "Total Habits: $totalHabits"
                binding.completedHabitsText.text = "Completed Habits: $completedHabits"
                if (totalHabits != 0) {
                    binding.progressBar.progress = (completedHabits * 100) / totalHabits
                } else {
                    binding.progressBar.progress = 0 // أو إظهار رسالة للمستخدم
                    Toast.makeText(requireContext(), "No habits available", Toast.LENGTH_SHORT).show()
                }
                binding.bestHabitText.text = "Best Habit: ${habits.firstOrNull() ?: "No habits"}"
                binding.achievementsText.text = "Achievements: ${completedHabits} habits completed"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading habits", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setUpChart() {
        val chart = binding.barChart
        val data = listOf(
            "Mon" to 3f,
            "Tue" to 5f,
            "Wed" to 2f,
            "Thu" to 4f,
            "Fri" to 6f
        )
        chart.animation.duration = 1000L
        chart.animate(data)
    }

    // هذه الدالة تقوم بتحديث حالة العادة في Firestore
    fun updateHabitCompletion(habitName: String, isCompleted: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val habitRef = db.collection("users").document(uid).collection("habits").document(habitName)

        habitRef.update("isCompleted", isCompleted)
            .addOnSuccessListener {
                loadHabitsFromFirestore() // إعادة تحميل العادات بعد التحديث
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error updating habit", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
