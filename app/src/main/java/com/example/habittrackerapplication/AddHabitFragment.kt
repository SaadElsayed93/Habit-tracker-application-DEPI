package com.example.habittrackerapplication

import HabitAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.example.habittrackerapplication.databinding.FragmentAddHabitBinding
import com.example.habittrackerapplication.databinding.DialogAddHabitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private lateinit var habitAdapter: HabitAdapter
    private val habitList = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        habitAdapter = HabitAdapter(habitList)
        binding.habitRecyclerView.adapter = habitAdapter

        // On Click of Add Habit button, show Add Habit dialog
        binding.addHabitButton.setOnClickListener {
            showAddHabitDialog()
        }
    }

    // Function to display the dialog for adding a new habit
    private fun showAddHabitDialog() {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val name = dialogBinding.etHabitName.text.toString().trim()
                val description = dialogBinding.etHabitDescription.text.toString().trim()
                val targetValueStr = dialogBinding.editTargetValue.text.toString().trim()

                if (name.isNotBlank() && description.isNotBlank()) {
                    val target = targetValueStr.toIntOrNull() ?: 1
                    addNewHabit(name, description, target)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun addNewHabit(name: String, description: String, target: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val habitId = FirebaseFirestore.getInstance().collection("users")
                .document(userId).collection("habits").document().id

            val newHabit = Habit(name, description, false, habitId, 0, target)

            FirebaseFirestore.getInstance().collection("users")
                .document(userId).collection("habits").document(habitId)
                .set(
                    mapOf(
                        "habitName" to name,
                        "habitDescription" to description,
                        "isCompleted" to false,
                        "currentProgress" to 0,
                        "target" to target
                    )
                )
                .addOnSuccessListener {
                    habitList.add(newHabit)
                    habitAdapter.notifyItemInserted(habitList.size - 1)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add habit.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

