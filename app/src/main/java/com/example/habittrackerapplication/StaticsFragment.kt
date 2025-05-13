package com.example.habittrackerapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
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
                val habits = mutableListOf<Habit>()
                result.forEach { document ->
                    totalHabits++
                    val isCompleted = document.getBoolean("isCompleted") ?: false
                    val habitName = document.getString("habitName") ?: ""
                    val habitDescription = document.getString("habitDescription") ?: ""
                    val currentValue = document.getLong("currentValue")?.toInt() ?: 0
                    val targetValue = document.getLong("target")?.toInt() ?: 1
                    val habitId = document.id

                    if (isCompleted) {
                        completedHabits++
                        habits.add(Habit(habitName, habitDescription, isCompleted, habitId, currentValue, targetValue))
                    }

                }

                binding.totalHabitsText.text = getString(R.string.total_habits, totalHabits)
                binding.completedHabitsText.text = getString(R.string.completed_habits, completedHabits)

                val progressPercentage = if (totalHabits != 0) {
                    (completedHabits * 100) / totalHabits
                } else {
                    0
                }

                binding.progressIndicator.progress = progressPercentage

                val bestHabit = habits.maxByOrNull { it.targetValue } ?: Habit("", "", false, "", 0, 0)
                binding.bestHabitText.text = if (bestHabit.name.isNotEmpty()) {
                    getString(R.string.best_habit, bestHabit.name)
                } else {
                    getString(R.string.no_habits)
                }

                binding.achievementsText.text = getString(R.string.achievements, completedHabits)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.error_loading), Toast.LENGTH_SHORT).show()
            }
    }



    private fun setUpChart() {
        val chart = binding.barChart
        val data = mutableListOf<Pair<String, Float>>()

        val habitData = mapOf(
            getString(R.string.sunday_short) to 2f,
            getString(R.string.monday_short) to 3f,
            getString(R.string.tuesday_short) to 5f,
            getString(R.string.wednesday_short) to 2f,
            getString(R.string.thursday_short) to 4f,
            getString(R.string.friday_short) to 6f,
            getString(R.string.saturday_short) to 1f
        )

        val isRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        val orderedData = if (isRtl) habitData.entries.reversed() else habitData.entries

        orderedData.forEach { (day, count) ->
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
