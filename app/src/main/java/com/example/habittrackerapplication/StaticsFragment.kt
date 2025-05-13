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

        loadHabitsFromFirestore()
        setUpChart()
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

                binding.totalHabitsText.text =
                    getString(R.string.total_habits, totalHabits)

                binding.completedHabitsText.text =
                    getString(R.string.completed_habits, completedHabits)

                val progressPercentage = if (totalHabits != 0) {
                    (completedHabits * 100) / totalHabits
                } else {
                    0
                }

                binding.progressIndicator.progress = progressPercentage  // تعيين النسبة المئوية هنا

                val bestHabit = habits.firstOrNull() ?: getString(R.string.no_habits)
                binding.bestHabitText.text =
                    getString(R.string.best_habit, bestHabit)

                binding.achievementsText.text =
                    getString(R.string.achievements, completedHabits)
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_loading),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun setUpChart() {
        val chart = binding.barChart
        val data = mutableListOf<Pair<String, Float>>()

        // تحديث البيانات بناءً على العادات المكتملة في أيام الأسبوع
        val habitData = mapOf(
            getString(R.string.monday_short) to 3f,
            getString(R.string.tuesday_short) to 5f,
            getString(R.string.wednesday_short) to 2f,
            getString(R.string.thursday_short) to 4f,
            getString(R.string.friday_short) to 6f
        )

        habitData.forEach { (day, count) ->
            data.add(day to count)
        }

        chart.animation.duration = 1000L
        chart.animate(data)
    }

    fun updateHabitCompletion(habitName: String, isCompleted: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val habitRef = db.collection("users").document(uid)
            .collection("habits").document(habitName)

        habitRef.update("isCompleted", isCompleted)
            .addOnSuccessListener {
                loadHabitsFromFirestore()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_updating),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
