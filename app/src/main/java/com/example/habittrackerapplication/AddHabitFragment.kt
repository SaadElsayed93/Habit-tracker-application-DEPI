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
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etHabitName.text.toString()
                val description = dialogBinding.etHabitDescription.text.toString()

                // Validate inputs before adding
                if (name.isNotBlank() && description.isNotBlank()) {
                    addNewHabit(name, description)
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    // Function to add a new habit to the list and notify the adapter
    private fun addNewHabit(name: String, description: String) {
        habitList.add(Habit(name, description, false,""))
        habitAdapter.notifyItemInserted(habitList.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
